package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Teacher;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RevokeAccessLevelTest extends BaseTestSetup {

    @TestConfiguration
    static class SyncAsyncTestConfig {
        @Bean
        @Primary
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

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

    @MockitoBean
    private EmailService emailService;

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
        return createTestAccountWithRoleState(prefix, roleName, true);
    }

    private Account createTestAccountWithRoleState(String prefix, String roleName, boolean isActive) {
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
            accessLevel.setActive(isActive);
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

        requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                new Headers(new Header("if-Match", validHash))
        );

        verify(emailService, times(1)).sendAccessLevelRevokedInfo(
                eq(testAccount.getEmail()),
                eq(testAccount.getLogin()),
                eq(AccountConsts.ROLE_TEACHER), any()
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = "{\"versionHash\": \"any-hash\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/api/account/" + nonExistentId + "/access-levels/" + AccountConsts.ROLE_STUDENT + "/revoke",
                new Headers(new Header("if-Match", "any-hash"))
        );

        verify(emailService, never()).sendAccessLevelRevokedInfo(any(), any(), any(), any());
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
                new Headers(new Header("if-Match", invalidHash))
        );

        verify(emailService, never()).sendAccessLevelRevokedInfo(any(), any(), any(), any());
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
                401,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                new Headers(new Header("if-Match", validHash)));
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        Account testAccount = createTestAccountWithRole("priv_rev", AccountConsts.ROLE_TEACHER);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";
        Headers headers = new Headers(studentHeaders.get("Authorization"), new Header("if-Match", validHash));
        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                headers
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
                new Headers(new Header("if-Match", validHash))
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
                new Headers(new Header("if-Match", validHash))
        );
    }

    @Test
    @DisplayName("Odrzucenie próby odebrania poziomu dostępu, który jest już odebrany")
    public void shouldFailWhenRoleAlreadyRevoked() {
        Account testAccount = createTestAccountWithRoleState("alrd_rev", AccountConsts.ROLE_TEACHER, false);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/" + AccountConsts.ROLE_TEACHER + "/revoke",
                new Headers(new Header("if-Match", validHash))
        );
    }
}