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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailRestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GrantAccessLevelTest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountRepository;

    @Autowired
    private JwtAuthService jwtAuthService;

    @Autowired
    private VersionHashService versionHashService;

    @MockitoBean
    private EmailRestClient emailRestClient;

    private RequestFunctions requestFunctions;
    private Headers adminHeaders;
    private Headers studentHeaders;
    private Headers unauthenticatedHeaders;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        String adminToken = jwtAuthService.generateToken("admin", List.of(AccountConsts.ROLE_ADMIN), false);
        String studentToken = jwtAuthService.generateToken("student", List.of("STUDENT"), false);
//        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
//        studentHeaders = new Headers(new Header("Authorization", "Bearer " + studentToken));
        unauthenticatedHeaders = new Headers();
        requestFunctions = new RequestFunctions();
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
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        String responseBody = requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash))
        );

        String returnedLogin = JsonPath.from(responseBody).getString("account.login");
        List<Boolean> activeFlags = JsonPath.from(responseBody).getList("accessLevels.active", Boolean.class);

        assertAll(
                () -> assertEquals(testAccount.getLogin(), returnedLogin),
                () -> assertTrue(activeFlags.contains(true)),
                () -> verify(emailRestClient, times(1)).sendAccessLevelGrantedInfo(
                        eq(testAccount.getEmail()),
                        eq(testAccount.getLogin()),
                        eq("TEACHER"), any()
                )
        );
    }

    @Test
    @DisplayName("Pomyślne ponowne nadanie uprawnień, które zostały wcześniej odebrane (aktywacja flagi)")
    public void shouldReactivateRevokedAccessLevelSuccessfully() {
        Account testAccount = createTestAccount("regrant");
        UUID accountId = testAccount.getId();

        String validHash1 = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String reqGrant1 = "{\"versionHash\": \"" + validHash1 + "\"}";
        requestFunctions.sendPostRequest(reqGrant1, 200,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash1)));

        Account accAfterGrant = accountRepository.findById(accountId).get();
        String validHash2 = versionHashService.hashObjectVersion(accountId, accAfterGrant.getVersion());
        String reqRevoke = "{\"versionHash\": \"" + validHash2 + "\"}";
        requestFunctions.sendPostRequest(reqRevoke,
                200,
                "/api/account/" + accountId + "/access-levels/TEACHER/revoke",
                new Headers(new Header("if-Match", validHash2)));

        Account accAfterRevoke = accountRepository.findById(accountId).get();
        String validHash3 = versionHashService.hashObjectVersion(accountId, accAfterRevoke.getVersion());
        String reqGrant2 = "{\"versionHash\": \"" + validHash3 + "\"}";
        String responseBody = requestFunctions.sendPostRequest(reqGrant2,
                200,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash3)));

        List<Boolean> activeFlags = JsonPath.from(responseBody).getList("accessLevels.active", Boolean.class);
        assertTrue(activeFlags.contains(true));
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String invalidHash = "mock-hash";
        String requestBody = "{\"versionHash\": \"" + invalidHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/api/account/" + nonExistentId + "/access-levels/STUDENT/grant",
                new Headers(new Header("if-Match", invalidHash))
        );

        verify(emailRestClient, never()).sendAccessLevelGrantedInfo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla przestarzałej wersji obiektu")
    public void shouldFailForWrongVersion() {
        Account testAccount = createTestAccount("ver");
        UUID accountId = testAccount.getId();
        String invalidHash = "invalid-hash-12345";
        String requestBody = "{\"versionHash\": \"" + invalidHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/ADMINISTRATOR/grant",
                new Headers(new Header("if-Match", invalidHash))
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu bez ważnego tokena uwierzytelniającego")
    public void shouldFailWithoutToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        Account testAccount = createTestAccount("auth");
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                401,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                unauthenticatedHeaders
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu z uprawnieniami studenta zamiast administratora")
    public void shouldFailWithStudentToken() {
        ((FilterableRequestSpecification) RestAssured.requestSpecification).removeHeader("Authorization");
        Account testAccount = createTestAccount("priv");
        String studentToken = jwtAuthService.generateToken(testAccount.getLogin(), List.of(AccountConsts.ROLE_STUDENT), false);
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                403,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash), new Header("Authorization", "Bearer " + studentToken))
        );
    }

    @Test
    @DisplayName("Odrzucenie próby przypisania poziomu dostępu, który użytkownik już posiada jako aktywny")
    public void shouldFailWhenRoleAlreadyAssigned() {
        Account testAccount = createTestAccount("dup");
        UUID accountId = testAccount.getId();
        String validHash1 = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody1 = "{\"versionHash\": \"" + validHash1 + "\"}";

        requestFunctions.sendPostRequest(
                requestBody1,
                200,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash1))
        );

        Account updatedAccount = accountRepository.findById(accountId).get();
        String validHash2 = versionHashService.hashObjectVersion(accountId, updatedAccount.getVersion());
        String requestBody2 = "{\"versionHash\": \"" + validHash2 + "\"}";

        requestFunctions.sendPostRequest(
                requestBody2,
                409,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                new Headers(new Header("if-Match", validHash2))
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu, który nie istnieje w słowniku")
    public void shouldFailForNonExistentRole() {
        Account testAccount = createTestAccount("role");
        UUID accountId = testAccount.getId();
        String validHash = versionHashService.hashObjectVersion(accountId, testAccount.getVersion());
        String requestBody = "{\"versionHash\": \"" + validHash + "\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                409,
                "/api/account/" + accountId + "/access-levels/SUPER_HERO/grant",
                new Headers(new Header("if-Match", validHash))
        );
    }

    @TestConfiguration
    static class SyncAsyncTestConfig {
        @Bean
        @Primary
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }
}