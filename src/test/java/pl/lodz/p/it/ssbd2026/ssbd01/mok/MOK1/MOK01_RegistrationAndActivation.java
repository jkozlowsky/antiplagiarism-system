package pl.lodz.p.it.ssbd2026.ssbd01.mok.MOK1;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Student;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.EmailService;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.config.BaseTestSetup;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.functions.RequestFunctions;
import pl.lodz.p.it.ssbd2026.ssbd01.utils.repositories.AccountTestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private org.springframework.core.env.Environment env;

    private RequestFunctions requestFunctions;

    @BeforeEach
    public void setupTest() {
        int actualPort = Integer.parseInt(env.getProperty("local.server.port", "8080"));
        io.restassured.RestAssured.port = actualPort;
        io.restassured.RestAssured.requestSpecification.port(actualPort);
        io.restassured.RestAssured.baseURI = "http://localhost";
        io.restassured.RestAssured.basePath = "/api";
        requestFunctions = new RequestFunctions();
    }

    @Test
    @DisplayName("POS01: Powinien poprawnie zarejestrować i aktywować konto")
    public void POS01_shouldCompleteFullRegistrationAndActivationFlow() {

        String requestBody = """
                {
                  "firstName": "Jan",
                  "lastName": "Testowy",
                  "email": "jan.testowy.api@example.com",
                  "login": "jantestowy123",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;

        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 202, "/account/register", headers);

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

        verify(emailService, times(1)).sendActivationEmail(eq("jan.testowy.api@example.com"), eq(token));

        String activationUrl = "/account/activate?token=" + token;
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
    public void POS02_scheduler_ShouldSendReminderAfter12h() {
        String requestBody = """
                {
                  "firstName": "Test",
                  "lastName": "Reminder",
                  "email": "reminder@example.com",
                  "login": "reminder123",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        String token = txTemplate.execute(status -> {
            Account account = accountTestRepository.findByLogin("reminder123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(13));
            accountTestRepository.save(account);
            return account.getActivationToken();
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendActivationReminderEmail(eq("reminder@example.com"), eq(token));

        txTemplate.executeWithoutResult(status -> {
            Account updatedAccount = accountTestRepository.findByLogin("reminder123").get();
            assertTrue(updatedAccount.isSecondEmailSent(), "Flaga secondEmailSent powinna zmienić się na true");
            assertFalse(updatedAccount.getIsVerified());
        });
    }

    @Test
    @DisplayName("POS03: Scheduler powinien usunąć konto po 24h od rejestracji")
    public void POS03_scheduler_ShouldDeleteAccountAfter24h() {
        String requestBody = """
                {
                  "firstName": "Test",
                  "lastName": "Delete",
                  "email": "delete@example.com",
                  "login": "delete123",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);

        txTemplate.executeWithoutResult(status -> {
            Account account = accountTestRepository.findByLogin("delete123").get();
            account.setCreationDate(java.time.LocalDateTime.now().minusHours(25));
            account.setSecondEmailSent(true);
            accountTestRepository.save(account);
        });

        accountScheduler.processInactiveAccounts();

        verify(emailService, times(1)).sendAccountDeletedEmail(eq("delete@example.com"));

        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("delete123").isEmpty(), "Konto powinno zostać fizycznie usunięte z bazy danych");
        });
    }

    @Test
    @DisplayName("NEG01: Rejestracja powinna zwrócić błąd (400), gdy hasła się nie zgadzają")
    public void NEG01_register_ShouldReturn400_WhenPasswordsMismatch() {
        String requestBody = """
                {
                  "firstName": "Jan",
                  "lastName": "Testowy",
                  "email": "janek@example.com",
                  "login": "janek123",
                  "password": "Password123!",
                  "confirmPassword": "InnePassword321!" 
                }
                """;
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        requestFunctions.sendPostRequest(requestBody, 400, "/account/register", headers);

        assertTrue(accountTestRepository.findByLogin("janek123").isEmpty());
    }

    @Test
    @DisplayName("NEG02: Rejestracja powinna zwrócić konflikt (409), gdy login jest już zajęty")
    public void NEG02_register_ShouldReturn409_WhenLoginAlreadyExists() {
        String firstUserJson = """
                { 
                  "firstName": "Anna", 
                  "lastName": "Kowalska", 
                  "email": "anna@example.com", 
                  "login": "zajetylogin", 
                  "password": "Password123!", 
                  "confirmPassword": "Password123!" 
                }
                """;
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(firstUserJson, 202, "/account/register", headers);

        String secondUserJson = """
                { 
                  "firstName": "Celina", 
                  "lastName": "Dabrowska", 
                  "email": "celina@example.com", 
                  "login": "zajetylogin", 
                  "password": "Password123!", 
                  "confirmPassword": "Password123!" 
                }
                """;

        requestFunctions.sendPostRequest(secondUserJson, 409, "/account/register", headers);
    }

    @Test
    @DisplayName("NEG03: Aktywacja powinna zwrócić błąd (400), gdy podano niewłaściwy token")
    public void NEG03_activate_ShouldReturn400_WhenTokenIsInvalid() {
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        String invalidActivationUrl = "/account/activate?token=zmyslony-falszywy-token-123";

        requestFunctions.sendPostRequest("", 400, invalidActivationUrl, headers);
    }

    @Test
    @DisplayName("NEG04: System powinien zablokować współbieżną próbę aktywacji tego samego konta (Optimistic Locking)")
    public void NEG04_activate_ShouldHandleRaceCondition() throws InterruptedException {
        String requestBody = """
                {
                  "firstName": "Race",
                  "lastName": "Condition",
                  "email": "race@example.com",
                  "login": "race123",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;
        Headers headers = new Headers(new Header("Content-Type", "application/json"));
        requestFunctions.sendPostRequest(requestBody, 202, "/account/register", headers);

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        String token = txTemplate.execute(status -> {
            return accountTestRepository.findByLogin("race123").get().getActivationToken();
        });

        String activationUrl = "/account/activate?token=" + token;

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
    public void NEG05_register_ShouldReturn400_WhenDataIsInvalidOrContainsSQLi() {
        Headers headers = new Headers(new Header("Content-Type", "application/json"));

        String badRequestBody = """
                {
                  "firstName": "Hacker",
                  "lastName": "SQL",
                  "email": "to-nie-jest-email",
                  "login": "'; DROP TABLE account; --",
                  "password": "Password123!",
                  "confirmPassword": "Password123!"
                }
                """;

        io.restassured.response.Response response = io.restassured.RestAssured.given()
                .headers(headers)
                .body(badRequestBody)
                .post("/account/register");

        assertNotEquals(202, response.getStatusCode(), "Żądanie z błędnymi danymi powinno zostać odrzucone");

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(adminTransactionManager);
        txTemplate.executeWithoutResult(status -> {
            assertTrue(accountTestRepository.findByLogin("'; DROP TABLE account; --").isEmpty(),
                    "Złośliwe konto NIE powinno zostać zarejestrowane");
        });
    }

}
