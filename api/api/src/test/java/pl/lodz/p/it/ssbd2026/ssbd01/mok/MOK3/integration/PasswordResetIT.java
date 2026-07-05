package pl.lodz.p.it.ssbd2026.ssbd01.mok.MOK3.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ConfirmResetDto;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.LanguageDict;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailRestClient;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.CommonFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PasswordResetIT extends BaseTestSetup {

    @Autowired
    private AccountRepository accountRepository;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailRestClient emailRestClient;

    @MockitoBean
    private VersionHashService versionHashService;

    private final RequestFunctions requestFunctions = new RequestFunctions();
    private final CommonFunctions commonFunctions = new CommonFunctions();

    private LanguageDict defaultLanguageDict;
    @Autowired
    @Qualifier("mokTransactionManager") // TODO: usunąć kiedy będzie rejestracja
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setupDb() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Wrap repository calls in a transaction so MANDATORY propagation is satisfied
        transactionTemplate.execute(status -> {
            Account adminUser = accountRepository.findByLogin("admin").orElseThrow();
            defaultLanguageDict = adminUser.getLanguageVersion();
            return null;
        });
    }

    @Test
    void POS01_requestReset_ShouldReturn200_WhenEmailIsValid() throws Exception {
        String testEmail = commonFunctions.generateRandomEmail();
        createActiveAccount(testEmail, commonFunctions.generateRandomStringOfLength(10));

        ResetPasswordDTO requestDto = new ResetPasswordDTO(testEmail);
        String jsonBody = objectMapper.writeValueAsString(requestDto);

        requestFunctions.sendPostRequest(
                jsonBody,
                200,
                "/api/auth/reset/request",
                new Headers()
        );

        verify(emailRestClient).sendResetPasswordEmail(eq(testEmail), anyString(), any());
    }

    @Test
    void POS02_confirmReset_ShouldReturn200_WhenTokenAndHashAreValid() throws Exception {
        String testEmail = commonFunctions.generateRandomEmail();
        createActiveAccount(testEmail, commonFunctions.generateRandomStringOfLength(10));
//        Mockito.reset(emailService);
        ResetPasswordDTO requestDto = new ResetPasswordDTO(testEmail);
        requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(requestDto),
                200,
                "/api/auth/reset/request",
                new Headers()
        );

        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailRestClient).sendResetPasswordEmail(eq(testEmail), emailBodyCaptor.capture(), any());


        String rawToken = emailBodyCaptor.getValue();

        ConfirmResetDto confirmDto = new ConfirmResetDto(rawToken, "new_secure_passworD123.");
        String responseBody = requestFunctions.sendPostRequest(
                objectMapper.writeValueAsString(confirmDto),
                200,
                "/api/auth/reset/confirm",
                new Headers()
        );
        assertEquals("Password successfully changed.", responseBody);
    }

    private void createActiveAccount(String email, String login) {
        transactionTemplate.execute(status -> {
            Account newAccount = new Account();
            newAccount.setLogin(login);
            newAccount.setEmail(email);
            newAccount.setPasswordHash("testcontainers_hash");
            newAccount.setName("Test");
            newAccount.setSurname("User");
            newAccount.setIsBlocked(false);
            newAccount.setLanguageVersion(defaultLanguageDict);

            accountRepository.saveAndFlush(newAccount);
            return null;
        });
    } //TODO: zamienić na wywołanie metody rejestracji z UserService
}
