package pl.lodz.p.it.ssbd2026.ssbd01.mok.codelogin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.LoginWithEmailRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.VerifyEmailSignInRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class AuthWithEmailOnlyIntegrationTest extends BaseTestSetup {
    @Autowired
    private AccountTestRepository accountTestRepository;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private RequestFunctions requestFunctions;

    @Autowired
    @Qualifier("mokTransactionManager")
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Headers jsonHeaders;

    @BeforeEach
    public void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        jsonHeaders = new Headers(new Header("Content-Type", "application/json"), new Header("Accept-Language", "pl"));
    }

    @Test
    public void POS01_LoginWithEmail_FullFlow_Success() throws Exception {
        Account testAccount = accountTestRepository.findByLogin("anowak").orElseThrow();

        LoginWithEmailRequestDTO loginReq = new LoginWithEmailRequestDTO(testAccount.getEmail());
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(loginReq),
                HttpStatus.ACCEPTED.value(),
                "/api/auth/loginEmail",
                jsonHeaders
        );

        ArgumentCaptor<String> authCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmailOnlyAuthCode(eq(testAccount.getEmail()), authCodeCaptor.capture(), eq("pl"));
        String capturedAuthCode = authCodeCaptor.getValue();

        assertNotNull(capturedAuthCode);
        assertEquals(OneTimeTokenConsts.CODE_LOGIN_TOKEN_LENGTH, capturedAuthCode.length());

        VerifyEmailSignInRequestDTO verifyReq = new VerifyEmailSignInRequestDTO(capturedAuthCode, testAccount.getEmail());
        String response = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(verifyReq),
                HttpStatus.OK.value(),
                "/api/auth/checkEmail",
                jsonHeaders
        );

        String token = JsonPath.from(response).getString("token");
        String refreshToken = JsonPath.from(response).getString("refreshToken");

        assertAll(
                () -> assertNotNull(token),
                () -> assertNotNull(refreshToken)
        );

        transactionTemplate.executeWithoutResult(status -> {
            Account dbAccount = accountTestRepository.findByEmail(testAccount.getEmail()).orElseThrow();
            assertEquals(0, dbAccount.getFailedAttempts());
        });
    }

    @Test
    public void NEG01_LoginWithEmail_NonExistingEmail() throws Exception {
        LoginWithEmailRequestDTO req = new LoginWithEmailRequestDTO("nieistniejacy@example.com");

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(req),
                HttpStatus.NOT_FOUND.value(),
                "/api/auth/loginEmail",
                jsonHeaders
        );
    }

    @Test
    public void NEG02_LoginWithEmail_InvalidCode() throws Exception {
        Account testAccount = accountTestRepository.findByLogin("jkowalski").orElseThrow();

        LoginWithEmailRequestDTO loginReq = new LoginWithEmailRequestDTO(testAccount.getEmail());
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(loginReq),
                HttpStatus.ACCEPTED.value(),
                "/api/auth/loginEmail",
                jsonHeaders
        );

        VerifyEmailSignInRequestDTO verifyReq = new VerifyEmailSignInRequestDTO("WRONGCODE1", testAccount.getEmail());

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(verifyReq),
                HttpStatus.CONFLICT.value(),
                "/api/auth/checkEmail",
                jsonHeaders
        );

        transactionTemplate.executeWithoutResult(status -> {
            Account dbAccount = accountTestRepository.findByEmail(testAccount.getEmail()).orElseThrow();
            assertEquals(1, dbAccount.getFailedAttempts());
        });
    }

    @Test
    public void NEG03_LoginWithEmail_AccountIsBlocked() throws Exception {
        Account testAccount = transactionTemplate.execute(status -> {
            Account account = accountTestRepository.findByLogin("admin").orElseThrow();
            account.setIsBlocked(true);
            return accountTestRepository.save(account);
        });
        LoginWithEmailRequestDTO loginReq = new LoginWithEmailRequestDTO(testAccount.getEmail());

        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(loginReq),
                HttpStatus.CONFLICT.value(),
                "/api/auth/loginEmail",
                jsonHeaders
        );

        transactionTemplate.executeWithoutResult(status -> {
            Account account = accountTestRepository.findByLogin("admin").orElseThrow();
            account.setIsBlocked(false);
            accountTestRepository.save(account);
        });
    }
}