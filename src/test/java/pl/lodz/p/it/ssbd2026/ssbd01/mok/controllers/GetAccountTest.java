package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetAccountTest extends BaseTestSetup {

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

        String adminToken = jwtAuthService.generateToken("admin", List.of("ADMINISTRATOR"), 1);
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"), 1);

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
    @DisplayName("Pomyślne pobranie danych istniejącego konta z uprawnieniami administratora")
    public void shouldGetAccountSuccessfully() {
        Account testAccount = createTestAccount("get_user");
        UUID accountId = testAccount.getId();

        String responseBody = requestFunctions.sendGetRequest(
                200,
                "/api/account/" + accountId,
                adminHeaders
        );

        String returnedLogin = JsonPath.from(responseBody).getString("account.login");
        String returnedEmail = JsonPath.from(responseBody).getString("account.email");

        assertAll(
                () -> assertNotNull(returnedLogin),
                () -> assertEquals(testAccount.getLogin(), returnedLogin),
                () -> assertEquals(testAccount.getEmail(), returnedEmail)
        );
    }

    @Test
    @DisplayName("Odrzucenie próby pobrania danych nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();

        requestFunctions.sendGetRequest(
                404,
                "/account/" + nonExistentId,
                adminHeaders
        );
    }

    //TODO: poprawić tutaj headery bo ustawiane są dwa razy w testach i przechodzi kiedy ma wrzucić 403
    @Test
    @DisplayName("Odrzucenie próby pobrania profilu innego użytkownika bez uprawnień administratora")
    @Disabled
    public void shouldFailWithStudentToken() {
        Account testAccount = createTestAccount("get_priv");
        UUID accountId = testAccount.getId();

        requestFunctions.sendGetRequest(
                403,
                "/api/account/" + accountId,
                studentHeaders
        );
    }

    //TODO: poprawić tutaj headery bo ustawiane są dwa razy w testach i przechodzi kiedy ma wrzucić 403
    @Test
    @DisplayName("Odrzucenie próby pobrania profilu bez ważnego tokena uwierzytelniającego")
    @Disabled
    public void shouldFailWithoutToken() {
        Account testAccount = createTestAccount("get_auth");
        UUID accountId = testAccount.getId();

        requestFunctions.sendGetRequest(
                403,
                "/api/account/" + accountId,
                unauthenticatedHeaders
        );
    }
}