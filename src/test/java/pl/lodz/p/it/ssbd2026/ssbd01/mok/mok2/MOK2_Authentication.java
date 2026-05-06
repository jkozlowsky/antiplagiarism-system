package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.LoginRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RefreshTokenRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.AccessLevelDict;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Admin;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccessLevelDictRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MOK2_Authentication extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private AccessLevelDictRepository accessLevelDictRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CommonFunctions commonFunctions = new CommonFunctions();
    private Headers jsonHeaders;

    @BeforeEach
    public void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        jsonHeaders = new Headers(new Header("Content-Type", "application/json"));
    }

    private Account createTestAccount(String login, String password, boolean isBlocked, String roleName) {
        return transactionTemplate.execute(status -> {
            Account adminUser = accountTestRepository.findByLogin("admin").orElseThrow();
            Account account = new Account();
            account.setLogin(login);
            account.setEmail(login + "@example.com");
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setName("Test");
            account.setSurname("User");
            account.setIsBlocked(isBlocked);
            account.setIsVerified(true);
            account.setFailedAttempts(0);
            account.setLanguageVersion(adminUser.getLanguageVersion());
            account.setAccessLevelList(new ArrayList<>());

            AccessLevelDict dict = accessLevelDictRepository.findByAccessLevelName(roleName).orElseThrow();
            if (AccountConsts.ROLE_ADMIN.equals(roleName)) {
                Admin adminRole = new Admin();
                adminRole.setAccessLevelDict(dict);
                adminRole.setAccount(account);
                adminRole.setActive(true);
                account.getAccessLevelList().add(adminRole);
            } else {
                Student studentRole = new Student();
                studentRole.setAccessLevelDict(dict);
                studentRole.setAccount(account);
                studentRole.setActive(true);
                account.getAccessLevelList().add(studentRole);
            }

            return accountTestRepository.save(account);
        });
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie zalogować użytkownika, zresetować błędy i zwrócić tokeny")
    public void POS01_Login_Success() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        String password = "StrongPassword123!";
        createTestAccount(login, password, false, AccountConsts.ROLE_STUDENT);

        LoginRequestDTO req = new LoginRequestDTO(login, password);
        String response = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(req),
                200,
                "/api/auth/login",
                jsonHeaders
        );

        String token = JsonPath.from(response).getString("token");
        String refreshToken = JsonPath.from(response).getString("refreshToken");

        assertAll(
                () -> assertNotNull(token, "Zwrócony token nie może być pusty"),
                () -> assertNotNull(refreshToken, "Zwrócony refresh token nie może być pusty")
        );

        transactionTemplate.executeWithoutResult(status -> {
            Account dbAccount = accountTestRepository.findByLogin(login).orElseThrow();
            assertEquals(0, dbAccount.getFailedAttempts(), "Licznik błędów powinien zostać zresetowany");
            assertNotNull(dbAccount.getLastSuccessfulLoginIp(), "Powinno zarejestrować IP logowania");
            assertNotNull(dbAccount.getLastLoginSuccessDateTime(), "Powinno zaktualizować czas udanego logowania");
        });
    }

    @Test
    @DisplayName("POS02: Udane logowanie Administratora powinno wysłać powiadomienie e-mail")
    public void POS02_Login_Admin_SendsEmail() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        String password = "StrongPassword123!";
        createTestAccount(login, password, false, AccountConsts.ROLE_ADMIN);

        LoginRequestDTO req = new LoginRequestDTO(login, password);
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(req),
                200,
                "/api/auth/login",
                jsonHeaders
        );

        verify(emailService, times(1)).sendAdminLoginNotification(eq(login + "@example.com"), eq(login), anyString());
    }

    @Test
    @DisplayName("POS03: Powinien poprawnie odświeżyć token przy użyciu Refresh Tokenu")
    public void POS03_RefreshToken_Success() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        String password = "StrongPassword123!";
        createTestAccount(login, password, false, AccountConsts.ROLE_STUDENT);

        LoginRequestDTO loginReq = new LoginRequestDTO(login, password);
        String loginRes = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(loginReq),
                200,
                "/api/auth/login",
                jsonHeaders
        );
        String oldRefreshToken = JsonPath.from(loginRes).getString("refreshToken");

        RefreshTokenRequestDTO refreshReq = new RefreshTokenRequestDTO(oldRefreshToken);
        String refreshRes = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(refreshReq),
                200,
                "/api/auth/refresh",
                jsonHeaders
        );

        String newToken = JsonPath.from(refreshRes).getString("token");
        assertNotNull(newToken, "System powinien zwrócić nowy Access Token");
    }

    @Test
    @DisplayName("NEG01: Błędne hasło powinno zwrócić 401 i inkrementować licznik błędów")
    public void NEG01_Login_InvalidCredentials() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        createTestAccount(login, "CorrectPass123!", false, AccountConsts.ROLE_STUDENT);

        LoginRequestDTO req = new LoginRequestDTO(login, "WrongPass!!!");
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(req),
                401,
                "/api/auth/login",
                jsonHeaders
        );

        transactionTemplate.executeWithoutResult(status -> {
            Account dbAccount = accountTestRepository.findByLogin(login).orElseThrow();
            assertEquals(1, dbAccount.getFailedAttempts(), "Licznik błędnych prób powinien zostać inkrementowany");
            assertNotNull(dbAccount.getLastLoginFailureDateTime(), "Powinno zaktualizować czas nieudanego logowania");
        });
    }

    @Test
    @DisplayName("NEG02: System powinien automatycznie zablokować konto i wysłać maila po 3 błędnych logowaniach")
    public void NEG02_Login_AutoBlockAfter3Failures() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        createTestAccount(login, "CorrectPass123!", false, AccountConsts.ROLE_STUDENT);

        LoginRequestDTO req = new LoginRequestDTO(login, "WrongPass!!!");
        String requestJson = objectMapper.writeValueAsString(req);

        requestFunctions.sendPostRequest(requestJson, 401, "/api/auth/login", jsonHeaders);
        requestFunctions.sendPostRequest(requestJson, 401, "/api/auth/login", jsonHeaders);
        requestFunctions.sendPostRequest(requestJson, 401, "/api/auth/login", jsonHeaders);

        transactionTemplate.executeWithoutResult(status -> {
            Account dbAccount = accountTestRepository.findByLogin(login).orElseThrow();
            assertTrue(dbAccount.getIsBlocked(), "Konto powinno zostać automatycznie zablokowane");
            assertEquals(3, dbAccount.getFailedAttempts(), "Licznik prób logowania powinien wynosić 3");
        });

        verify(emailService, times(1)).sendAutoBlockEmail(eq(login + "@example.com"), eq(login));
    }

    @Test
    @DisplayName("NEG03: Logowanie na zablokowane konto powinno zostać odrzucone (409 Conflict)")
    public void NEG03_Login_AccountBlocked() throws Exception {
        String login = commonFunctions.generateRandomStringOfLength(10);
        String password = "StrongPassword123!";
        createTestAccount(login, password, true, AccountConsts.ROLE_STUDENT);

        LoginRequestDTO req = new LoginRequestDTO(login, password);
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(req),
                409,
                "/api/auth/login",
                jsonHeaders
        );
    }

    @Test
    @DisplayName("NEG04: Próba użycia nieważnego lub fałszywego Refresh Tokenu powinna zostać odrzucona (401 Unauthorized)")
    public void NEG04_RefreshToken_InvalidToken() throws Exception {
        RefreshTokenRequestDTO refreshReq = new RefreshTokenRequestDTO("falszywy.bledny.token");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(refreshReq),
                401,
                "/api/auth/refresh",
                jsonHeaders
        );
    }
}