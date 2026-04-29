package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GrantAccessLevelTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpAuth() {
        requestFunctions = new RequestFunctions();

        String adminToken = jwtAuthService.generateToken("admin", List.of("ADMINISTRATOR"));
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"));

        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
    }

    private Account createTestAccount(String prefix) {
        Account admin = accountRepository.findByLogin("admin").get();
        LanguageDict language = admin.getLanguageVersion();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        Account testAccount = new Account();
        testAccount.setLogin(prefix + "_" + uniqueId);
        testAccount.setEmail(prefix + "_" + uniqueId + "@example.com");
        testAccount.setPasswordHash("password_hash");
        testAccount.setName("TestName");
        testAccount.setSurname("TestSurname");
        testAccount.setIsBlocked(false);
        testAccount.setLanguageVersion(language);

        return accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Pomyślne nadanie uprawnień dla istniejącego konta")
    public void shouldGrantAccessLevelSuccessfully() {
        Account testAccount = createTestAccount("user");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        String responseBody = requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/account/" + accountId + "/access-levels/TEACHER/grant",
                adminHeaders
        );

        String returnedLogin = JsonPath.from(responseBody).getString("account.login");
        List<Boolean> activeFlags = JsonPath.from(responseBody).getList("accessLevels.active", Boolean.class);

        assertAll(
                () -> assertEquals(testAccount.getLogin(), returnedLogin),
                () -> assertTrue(activeFlags.contains(true))
        );
    }

    @Test
    @DisplayName("Pomyślne ponowne nadanie uprawnień, które zostały wcześniej odebrane (aktywacja flagi)")
    public void shouldReactivateRevokedAccessLevelSuccessfully() {
        Account testAccount = createTestAccount("regrant");
        UUID accountId = testAccount.getId();

        String reqGrant1 = "{\"version\": " + testAccount.getVersion() + "}";
        requestFunctions.sendPostRequest(reqGrant1, 200, "/account/" + accountId + "/access-levels/TEACHER/grant", adminHeaders);

        Account accAfterGrant = accountRepository.findById(accountId).get();
        String reqRevoke = "{\"version\": " + accAfterGrant.getVersion() + "}";
        requestFunctions.sendPostRequest(reqRevoke, 200, "/account/" + accountId + "/access-levels/TEACHER/revoke", adminHeaders);

        Account accAfterRevoke = accountRepository.findById(accountId).get();
        String reqGrant2 = "{\"version\": " + accAfterRevoke.getVersion() + "}";
        String responseBody = requestFunctions.sendPostRequest(reqGrant2, 200, "/account/" + accountId + "/access-levels/TEACHER/grant", adminHeaders);

        List<Boolean> activeFlags = JsonPath.from(responseBody).getList("accessLevels.active", Boolean.class);
        assertTrue(activeFlags.contains(true));
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = "{\"version\": 0}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/account/" + nonExistentId + "/access-levels/STUDENT/grant",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla przestarzałej wersji obiektu")
    public void shouldFailForWrongVersion() {
        Account testAccount = createTestAccount("ver");
        UUID accountId = testAccount.getId();
        Integer wrongVersion = testAccount.getVersion() + 1;
        String requestBody = "{\"version\": " + wrongVersion + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/account/" + accountId + "/access-levels/ADMINISTRATOR/grant",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu bez ważnego tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        Account testAccount = createTestAccount("auth");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/account/" + accountId + "/access-levels/TEACHER/grant",
                unauthenticatedHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        Account testAccount = createTestAccount("priv");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/account/" + accountId + "/access-levels/TEACHER/grant",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby przypisania poziomu dostępu, który użytkownik już posiada jako aktywny")
    public void shouldFailWhenRoleAlreadyAssigned() {
        Account testAccount = createTestAccount("dup");
        UUID accountId = testAccount.getId();
        String requestBody1 = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody1,
                200,
                "/account/" + accountId + "/access-levels/TEACHER/grant",
                adminHeaders
        );

        Account updatedAccount = accountRepository.findById(accountId).get();
        String requestBody2 = "{\"version\": " + updatedAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody2,
                409,
                "/account/" + accountId + "/access-levels/TEACHER/grant",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu, który nie istnieje w słowniku")
    public void shouldFailForNonExistentRole() {
        Account testAccount = createTestAccount("role");
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/account/" + accountId + "/access-levels/SUPER_HERO/grant",
                adminHeaders
        );
    }
}