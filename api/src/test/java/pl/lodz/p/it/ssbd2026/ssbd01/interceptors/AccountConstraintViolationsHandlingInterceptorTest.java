package pl.lodz.p.it.ssbd2026.ssbd01.interceptors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AccountConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountConstraintViolationsHandlingInterceptorTest {

    private AccountConstraintViolationsHandlingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AccountConstraintViolationsHandlingInterceptor();
    }

    @Test
    @DisplayName("Powinno rzucić AccountConstraintViolationException z kluczem loginu, gdy naruszono unikalność loginu")
    void shouldThrowExceptionForDuplicateLogin() {
        String errorMessage = "duplicate key value violates unique constraint \"" + Account.UNIQUE_LOGIN_INDEX_NAME + "\"";
        DataIntegrityViolationException dive = new DataIntegrityViolationException(errorMessage);

        AccountConstraintViolationException exception = assertThrows(
                AccountConstraintViolationException.class,
                () -> interceptor.handleDataIntegrityViolationException(dive)
        );

        assertEquals("409 CONFLICT \"error.account.login.exists\"", exception.getMessage());
    }

    @Test
    @DisplayName("Powinno rzucić AccountConstraintViolationException z kluczem emaila, gdy naruszono unikalność emaila")
    void shouldThrowExceptionForDuplicateEmail() {
        String errorMessage = "duplicate key value violates unique constraint \"" + Account.UNIQUE_EMAIL_INDEX_NAME + "\"";
        DataIntegrityViolationException dive = new DataIntegrityViolationException(errorMessage);

        AccountConstraintViolationException exception = assertThrows(
                AccountConstraintViolationException.class,
                () -> interceptor.handleDataIntegrityViolationException(dive)
        );

        assertEquals("409 CONFLICT \"error.account.email.exists\"", exception.getMessage());
    }

    @Test
    @DisplayName("Powinno przepakować nieznany błąd integralności do AccountConstraintViolationException")
    void shouldWrapUnknownDataIntegrityViolationException() {
        String errorMessage = "duplicate key value violates unique constraint \"unknown_index_name\"";
        DataIntegrityViolationException dive = new DataIntegrityViolationException(errorMessage);

        AccountConstraintViolationException exception = assertThrows(
                AccountConstraintViolationException.class,
                () -> interceptor.handleDataIntegrityViolationException(dive)
        );

        assertEquals(dive, exception.getCause());
    }

    @Test
    @DisplayName("Powinno poprawnie obsłużyć DataIntegrityViolationException z wiadomością o wartości null")
    void shouldHandleNullMessageInDataIntegrityViolationException() {
        DataIntegrityViolationException dive = new DataIntegrityViolationException(null);

        AccountConstraintViolationException exception = assertThrows(
                AccountConstraintViolationException.class,
                () -> interceptor.handleDataIntegrityViolationException(dive)
        );

        assertEquals(dive, exception.getCause());
    }
}