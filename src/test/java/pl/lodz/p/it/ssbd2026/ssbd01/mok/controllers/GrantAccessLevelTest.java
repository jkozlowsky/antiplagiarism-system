package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
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
import pl.lodz.p.it.ssbd2026.ssbd01.config.security.JwtAuthService;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
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
    private EmailService emailService;
    private RequestFunctions requestFunctions;
    private Headers adminHeaders;

    @BeforeEach
    public void setUpRestAssuredAndAuth() {
        String adminToken = jwtAuthService.generateToken("admin", List.of("ADMINISTRATOR"));
        adminHeaders = new Headers(new Header("Authorization", "Bearer " + adminToken));
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

        requestFunctions.sendPostRequest(
                requestBody,
                200,
                "/api/account/" + accountId + "/access-levels/TEACHER/grant",
                adminHeaders
        );

        assertAll(
                () -> verify(emailService, times(1)).sendAccessLevelGrantedInfo(
                        eq(testAccount.getEmail()),
                        eq(testAccount.getLogin()),
                        eq("TEACHER")
                )
        );
    }

    @Test
    @DisplayName("Odrzucenie próby nadania poziomu dostępu dla nieistniejącego konta")
    public void shouldFailForNonExistentAccount() {
        UUID nonExistentId = UUID.randomUUID();
        String requestBody = "{\"versionHash\": \"any-hash\"}";

        requestFunctions.sendPostRequest(
                requestBody,
                404,
                "/api/account/" + nonExistentId + "/access-levels/STUDENT/grant",
                adminHeaders
        );

        verify(emailService, never()).sendAccessLevelGrantedInfo(any(), any(), any());
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