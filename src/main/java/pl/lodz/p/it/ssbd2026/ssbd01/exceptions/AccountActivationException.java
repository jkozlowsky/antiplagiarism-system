package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany w przypadku problemów z aktywacją konta (np. wygasły lub nieprawidłowy token).
 */
public class AccountActivationException extends AppBaseException {

    public AccountActivationException(String key) {
        super(HttpStatus.BAD_REQUEST, key);
    }

    public AccountActivationException() {
        super(HttpStatus.BAD_REQUEST, "error.activation.invalid");
    }

    public AccountActivationException(Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "error.activation.invalid", cause);
    }
}