package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RevokeAccessLevelTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountRepository;

    @Autowired
    private AccessLevelDictRepository accessLevelDictRepository;

    @Autowired
    private AccessLevelRepository accessLevelRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpAuth() {
        requestFunctions = new RequestFunctions();

        String adminToken = jwtAuthService.generateToken("admin", List.of(AccountConsts.ROLE_ADMIN));
        String studentToken = jwtAuthService.generateToken("student", List.of(AccountConsts.ROLE_STUDENT));

        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
    }

    private Account createTestAccountWithRole(String prefix, String roleName) {
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
        testAccount = accountRepository.save(testAccount);

        if (roleName != null) {
            AccessLevelDict dict = accessLevelDictRepository.findByAccessLevelName(roleName).get();
            Teacher accessLevel = new Teacher();
            accessLevel.setAccount(testAccount);
            accessLevel.setAccessLevelDict(dict);
            accessLevelRepository.save(accessLevel);
        }

        return accountRepository.findById(testAccount.getId()).get();
    }

    @Test
    @DisplayName("Pomyślne odebranie uprawnień dla konta posiadającego daną rolę")
    public void shouldRevokeAccessLevelSuccessfully() {
        Account testAccount = createTestAccountWithRole("user_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        String responseBody = requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );

        List<Object> accessLevels = JsonPath.from(responseBody).getList("accessLevels");
        assertTrue(accessLevels.isEmpty());
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = "{\"version\": 0}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/account/" + nonExistentId + "/access-levels/" + AccountConsts.ROLE_STUDENT + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu dla przestarzałej wersji obiektu")
    public void shouldFailForWrongVersion() {
        Account testAccount = createTestAccountWithRole("ver_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        Integer wrongVersion = testAccount.getVersion() + 1;
        String requestBody = "{\"version\": " + wrongVersion + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu bez ważnego tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        Account testAccount = createTestAccountWithRole("auth_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                unauthenticatedHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        Account testAccount = createTestAccountWithRole("priv_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, którego użytkownik nie posiada")
    public void shouldFailWhenRoleNotAssigned() {
        Account testAccount = createTestAccountWithRole("none_rev", null);
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, który nie istnieje w słowniku")
    public void shouldFailForNonExistentRole() {
        Account testAccount = createTestAccountWithRole("role_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String requestBody = "{\"version\": " + testAccount.getVersion() + "}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/account/" + accountId + "/access-levels/SUPER_HERO/revoke",
                adminHeaders
        );
    }
}