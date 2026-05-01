package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy konto, które chcemy znaleźć nie istnieje
 * Zwraca status 404 Not found.
 */
public class AccountNotFound extends AppBaseException {
    public AccountNotFound() {
        super(HttpStatus.NOT_FOUND, "Account not found");
    }

    public AccountNotFound(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "Account not found", cause);
    }
}
