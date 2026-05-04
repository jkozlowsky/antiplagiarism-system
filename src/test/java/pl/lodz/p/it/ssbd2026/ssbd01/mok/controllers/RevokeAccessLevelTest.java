package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.FilterableRequestSpecification;
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
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Autowired
    private VersionHashService versionHashService;

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
            accessLevel.setActive(true);
            accessLevelRepository.save(accessLevel);
        }

        return accountRepository.findById(testAccount.getId()).get();
    }

    @Test
    @DisplayName("Pomyślne miękkie usunięcie uprawnień dla konta posiadającego daną rolę")
    public void shouldRevokeAccessLevelSuccessfully() {
        Account testAccount = createTestAccountWithRole("user_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        String responseBody = requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );

        List<Boolean> activeFlags = JsonPath.from(responseBody).getList("accessLevels.active", Boolean.class);
        assertTrue(activeFlags.contains(false));
        assertFalse(activeFlags.contains(true));
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, który został już wyłączony")
    public void shouldFailWhenRoleAlreadyRevoked() {
        Account testAccount = createTestAccountWithRole("twice_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();

        String validHash1 = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody1 = "{\"versionHash\": \"" + validHash1 + "\"}";
        requestFunctions.sendPostRequest(requestBody1, 200, "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke", adminHeaders);

        Account updatedAccount = accountRepository.findById(accountId).get();
        String validHash2 = versionHashService.hashObjectVersion(accountId, updatedAccount.getVersion());
        String requestBody2 = "{\"versionHash\": \"" + validHash2 + "\"}";

        requestFunctions.sendPostRequest(requestBody2, 409, "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke", adminHeaders);
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String invalidHash = "mock-hash";
        String requestBody = "{\"versionHash\": \"" + invalidHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/api/account/" + nonExistentId + "/access-levels/" + AccountConsts.ROLE_STUDENT + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu dla przestarzałej wersji obiektu")
    public void shouldFailForWrongVersion() {
        Account testAccount = createTestAccountWithRole("ver_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String invalidHash = "invalid-hash-12345";
        String requestBody = "{\"versionHash\": \"" + invalidHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu bez ważnego tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        Account testAccount = createTestAccountWithRole("auth_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                new Headers()
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        Account testAccount = createTestAccountWithRole("priv_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                studentHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, którego użytkownik nigdy nie posiadał")
    public void shouldFailWhenRoleNeverAssigned() {
        Account testAccount = createTestAccountWithRole("none_rev", null);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                adminHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, który nie istnieje w słowniku")
    public void shouldFailForNonExistentRole() {
        Account testAccount = createTestAccountWithRole("role_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/SUPER_HERO/revoke",
                adminHeaders
        );
    }
}