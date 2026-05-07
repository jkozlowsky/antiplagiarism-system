package pl.lodz.p.it.ssbd2026.ssbd01.mok.mok1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MOK01_RegistrationAndActivation extends BaseTestSetup {

    @Autowired
    private AccountTestRepository accountTestRepository;

    @Autowired
    private pl.lodz.p.it.ssbd2026.ssbd01.mok.schedulers.AccountScheduler accountScheduler;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    @Qualifier("adminTransactionManager")
    private org.springframework.transaction.PlatformTransactionManager adminTransactionManager;

    @Autowired
    private RequestFunctions requestFunctions;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POS01: Powinien poprawnie zarejestrować i aktywować konto")
    public void POS01_shouldCompleteFullRegistrationAndActivationFlow() throws Exception {

        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Jan");
        registerDto.setSurname("Testowy");
        registerDto.setEmail("jan.testowy.api@example.com");
        registerDto.setLogin("jantestowy123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        String token = txTemplate.execute(status -> {
            Optional<Account> savedAccountOpt = accountTestRepository.findByLogin("jantestowy123");
            assertTrue(savedAccountOpt.isPresent(), "Konto powinno zostać utworzone w bazie");

            Account savedAccount = savedAccountOpt.get();

            assertAll("Weryfikacja stanu konta po udanej rejestracji",
                    () -> assertFalse(savedAccount.getIsVerified(), "Konto po rejestracji musi być niezweryfikowane"),
                    () -> assertNotNull(savedAccount.getActivationToken(), "System musi wygenerować token aktywacyjny"),
                    () -> assertTrue(savedAccount.getAccessLevelList().stream().anyMatch(al -> al instanceof Student),
                            "Konto musi mieć przypisaną rolę Studenta")
            );
            return savedAccount.getActivationToken();
        });

        verify(emailService, times(1)).sendActivationEmail(eq("jan.testowy.api@example.com"), eq(token), any());

        String activationUrl = "/api/account/activate?token=" + token;
        requestFunctions.sendPostRequest("", 200, activationUrl, headers);

        txTemplate.executeWithoutResult(status -> {
            Account activatedAccount = accountTestRepository.findByLogin("jantestowy123").get();

            assertAll("Weryfikacja stanu konta po aktywacji",
                    () -> assertTrue(activatedAccount.getIsVerified(), "Konto musi zmienić status na zweryfikowane (true)"),
                    () -> assertNull(activatedAccount.getActivationToken(), "Token po użyciu musi zostać wyczyszczony na null")
            );
        });
    }

    @Test
    @DisplayName("POS02: Scheduler powinien wysłać przypomnienie po 12h od rejestracji")
    public void POS02_scheduler_ShouldSendReminderAfter12h() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Test");
        registerDto.setSurname("Reminder");
        registerDto.setEmail("reminder@example.com");
        registerDto.setLogin("reminder123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        String token = txTemplate.execute(status -> {
            Account account = accountTestRepository.findByLogin("reminder123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(13));
            accountTestRepository.save(account);
            return account.getActivationToken();
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendActivationReminderEmail(eq("reminder@example.com"), eq(token), any());

        txTemplate.executeWithoutResult(status -> {
            Account updatedAccount = accountTestRepository.findByLogin("reminder123").get();
            assertTrue(updatedAccount.isSecondEmailSent(), "Flaga secondEmailSent powinna zmienić się na true");
            assertFalse(updatedAccount.getIsVerified());
        });
    }

    @Test
    @DisplayName("POS03: Scheduler powinien usunąć konto po 24h od rejestracji")
    public void POS03_scheduler_ShouldDeleteAccountAfter24h() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Test");
        registerDto.setSurname("Delete");
        registerDto.setEmail("delete@example.com");
        registerDto.setLogin("delete123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        txTemplate.executeWithoutResult(status -> {
            Account account = accountTestRepository.findByLogin("delete123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(25));
            account.setSecondEmailSent(true);
            accountTestRepository.save(account);
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendAccountDeletedEmail(eq("delete@example.com"), any());

        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("delete123").isEmpty(), "Konto powinno zostać fizycznie usunięte z bazy danych");
        });
    }

    @Test
    @DisplayName("NEG01: Rejestracja powinna zwrócić błąd (400), gdy hasło nie spełnia polityki bezpieczeństwa")
    public void NEG01_register_ShouldReturn400_WhenPasswordIsTooWeak() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Jan");
        registerDto.setSurname("Testowy");
        registerDto.setEmail("janek@example.com");
        registerDto.setLogin("janek123");
        registerDto.setPassword("slabehaslo");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 400, "/api/account/register", headers);

        assertTrue(accountTestRepository.findByLogin("janek123").isEmpty());
    }

    @Test
    @DisplayName("NEG02: Rejestracja powinna zwrócić konflikt (409), gdy login jest już zajęty")
    public void NEG02_register_ShouldReturn409_WhenLoginAlreadyExists() throws Exception {
        RegisterAccountDTO firstUser = new RegisterAccountDTO();
        firstUser.setName("Anna");
        firstUser.setSurname("Kowalska");
        firstUser.setEmail("anna@example.com");
        firstUser.setLogin("zajetylogin");
        firstUser.setPassword("Password123!");

        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(firstUser), 202, "/api/account/register", headers);

        RegisterAccountDTO secondUser = new RegisterAccountDTO();
        secondUser.setName("Celina");
        secondUser.setSurname("Dabrowska");
        secondUser.setEmail("celina@example.com");
        secondUser.setLogin("zajetylogin");
        secondUser.setPassword("Password123!");

        requestFunctions.sendPostRequest(objectMapper.writeValueAsString(secondUser), 409, "/api/account/register", headers);
    }

    @Test
    @DisplayName("NEG03: Aktywacja powinna zwrócić błąd (400), gdy podano niewłaściwy token")
    public void NEG03_activate_ShouldReturn400_WhenTokenIsInvalid() {
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        String invalidActivationUrl = "/api/account/activate?token=zmyslony-falszywy-token-123";

        requestFunctions.sendPostRequest("", 400, invalidActivationUrl, headers);
    }

    @Test
    @DisplayName("NEG04: System powinien zablokować współbieżną próbę aktywacji tego samego konta (Optimistic Locking)")
    public void NEG04_activate_ShouldHandleRaceCondition() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Race");
        registerDto.setSurname("Condition");
        registerDto.setEmail("race@example.com");
        registerDto.setLogin("race123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        String token = txTemplate.execute(status -> {
            return accountTestRepository.findByLogin("race123").get().getActivationToken();
        });

        String activationUrl = "/api/account/activate?token=" + token;

        int threadCount = 2;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger conflictCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    io.restassured.response.Response response = io.restassured.RestAssured.given()
                            .headers(headers)
                            .post(activationUrl);

                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                    } else if (response.getStatusCode() == 409 || response.getStatusCode() == 400 || response.getStatusCode() == 500) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Dokładnie jedno żądanie powinno zakończyć się sukcesem (200 OK)");
        assertEquals(1, conflictCount.get(), "Drugie żądanie powinno zostać odrzucone z powodu wyścigu (zmiana wersji)");
    }

    @Test
    @DisplayName("NEG05: Rejestracja powinna odrzucić próbę SQL Injection oraz niepoprawne formaty danych (400 Bad Request)")
    public void NEG05_register_ShouldReturn400_WhenDataIsInvalidOrContainsSQLi() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Hacker");
        registerDto.setSurname("SQL");
        registerDto.setEmail("to-nie-jest-email");
        registerDto.setLogin("'; DROP TABLE account; --");
        registerDto.setPassword("Password123!");

        String badRequestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        io.restassured.response.Response response = io.restassured.RestAssured.given()
                .headers(headers)
                .body(badRequestBody)
                .post("/api/account/register");

        assertNotEquals(202, response.getStatusCode(), "Żądanie z błędnymi danymi powinno zostać odrzucone (zły mail i login)");

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("'; DROP TABLE account; --").isEmpty(),
                    "Złośliwe konto NIE powinno zostać zarejestrowane");
        });
    }

    @Test
    @DisplayName("NEG06: System powinien odrzucić próbę aktywacji już aktywnego konta (Replay Attack)")
    public void NEG06_activate_ShouldReturn400_WhenAccountIsAlreadyActivated() throws Exception {
        RegisterAccountDTO registerDto = new RegisterAccountDTO();
        registerDto.setName("Replay");
        registerDto.setSurname("Attack");
        registerDto.setEmail("replay@example.com");
        registerDto.setLogin("replay123");
        registerDto.setPassword("Password123!");

        String requestBody = objectMapper.writeValueAsString(registerDto);
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/api/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        String token = txTemplate.execute(status -> {
            return accountTestRepository.findByLogin("replay123").get().getActivationToken();
        });

        String activationUrl = "/api/account/activate?token=" + token;

        requestFunctions.sendPostRequest("", 200, activationUrl, headers);

        requestFunctions.sendPostRequest("", 400, activationUrl, headers);
    }
}