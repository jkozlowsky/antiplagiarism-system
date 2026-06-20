package pl.lodz.p.it.ssbd2026.ssbd01.mok.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.LoginRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.Resend2FARequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.Verify2FARequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.*;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.OneTimeTokenRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class Authentication2FATest extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private AccessLevelDictRepository accessLevelDictRepository;

    @Autowired
    private OneTimeTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CommonFunctions commonFunctions = new CommonFunctions();
    private Headers jsonHeaders;

    @BeforeEach
    public void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        jsonHeaders = new Headers(new Header("Content-Type", "application/json"));
    }

    private Account createTestAccount(String login, String password, boolean isBlocked, boolean isVerified) {
        return transactionTemplate.execute(status -> {
            Account adminUser = accountTestRepository.findByLogin("admin").orElseThrow();
            Account account = new Account();
            account.setLogin(login);
            account.setEmail(login + "@example.com");
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setName("Test");
            account.setSurname("User");
            account.setIsBlocked(isBlocked);
            account.setIsVerified(isVerified);
            account.setFailedAttempts(0);
            account.setLanguageVersion(adminUser.getLanguageVersion());
            account.setAccessLevelList(new ArrayList<>());

            AccessLevelDict dict = accessLevelDictRepository.findByAccessLevelName(AccountConsts.ROLE_STUDENT).orElseThrow();
            Student studentRole = new Student();
            studentRole.setAccessLevelDict(dict);
            studentRole.setAccount(account);
            studentRole.setActive(true);
            account.getAccessLevelList().add(studentRole);

            return accountTestRepository.save(account);
        });
    }

    @Test
    @DisplayName("POS01: Udane uwierzytelnienie 2FA po prawidłowym podaniu 8-cyfrowego kodu")
    public void POS01_Verify2FA_Success() throws Exception {
        String login = "user" + commonFunctions.generateRandomStringOfLength(5);
        String password = "StrongPassword123!";
        createTestAccount(login, password, false, true);

        LoginRequestDTO loginReq = new LoginRequestDTO(login, password);
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(loginReq), 202, "/api/auth/login", jsonHeaders);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).send2FAuthEmail(tokenCaptor.capture(), eq(login + "@example.com"), any());
        String validCode = tokenCaptor.getValue();

        assertTrue(validCode.matches("^\\d{8}$"));

        Verify2FARequestDTO verifyReq = new Verify2FARequestDTO(login, validCode);
        String response = requestFunctions.sendPostRequest(objectMapper.writeValueAsString(verifyReq), 200, "/api/auth/check2FA", jsonHeaders);

        assertNotNull(JsonPath.from(response).getString("token"));
    }

    @Test
    @DisplayName("NEG01: Próba użycia kodu przypisanego do innego użytkownika")
    public void NEG01_Verify2FA_CrossUserToken() throws Exception {
        String userA = "userA" + commonFunctions.generateRandomStringOfLength(5);
        String userB = "userB" + commonFunctions.generateRandomStringOfLength(5);
        createTestAccount(userA, "Password123!", false, true);
        createTestAccount(userB, "Password123!", false, true);

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new LoginRequestDTO(userA, "Password123!")), 202, "/api/auth/login", jsonHeaders);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send2FAuthEmail(tokenCaptor.capture(), eq(userA + "@example.com"), any());
        String codeForA = tokenCaptor.getValue();

        Verify2FARequestDTO verifyReq = new Verify2FARequestDTO(userB, codeForA);
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(verifyReq), 409, "/api/auth/check2FA", jsonHeaders);
    }

    @Test
    @DisplayName("NEG02: Atak typu Replay")
    public void NEG02_Verify2FA_ReplayAttack() throws Exception {
        String login = "replay" + commonFunctions.generateRandomStringOfLength(5);
        createTestAccount(login, "Password123!", false, true);

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new LoginRequestDTO(login, "Password123!")), 202, "/api/auth/login", jsonHeaders);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send2FAuthEmail(tokenCaptor.capture(), anyString(), any());
        String code = tokenCaptor.getValue();

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new Verify2FARequestDTO(login, code)), 200, "/api/auth/check2FA", jsonHeaders);
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new Verify2FARequestDTO(login, code)), 409, "/api/auth/check2FA", jsonHeaders);
    }

    @Test
    @DisplayName("NEG03: Identity Enumeration w resend2FA")
    public void NEG03_Resend2FA_NonExistentUser() throws Exception {
        Resend2FARequestDTO resendReq = new Resend2FARequestDTO("nonexistent_user_999");
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(resendReq), 202, "/api/auth/resend2FA", jsonHeaders);
        verify(emailService, never()).send2FAuthEmail(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("NEG04: Brute-force kodu 2FA")
    public void NEG04_Verify2FA_BruteForce() throws Exception {
        String login = "bruteforce" + commonFunctions.generateRandomStringOfLength(5);
        createTestAccount(login, "Password123!", false, true);

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new LoginRequestDTO(login, "Password123!")), 202, "/api/auth/login", jsonHeaders);

        Verify2FARequestDTO wrongReq = new Verify2FARequestDTO(login, "00000000");
        for (int i = 0; i < 3; i++) {
            requestFunctions.sendPostRequest(objectMapper.writeValueAsString(wrongReq), 409, "/api/auth/check2FA", jsonHeaders);
        }
    }

    @Test
    @DisplayName("NEG05: Walidacja formatu kodu")
    public void NEG05_Verify2FA_ValidationRegex() throws Exception {
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new Verify2FARequestDTO("login", "123")), 400, "/api/auth/check2FA", jsonHeaders);
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new Verify2FARequestDTO("login", "123456789")), 400, "/api/auth/check2FA", jsonHeaders);
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(new Verify2FARequestDTO("login", "abcdefgh")), 400, "/api/auth/check2FA", jsonHeaders);
    }
}