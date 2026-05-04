package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy konto, które chcemy zablokować jest już zablokowane
 * Zwraca status 409 Conflict.
 */
public class AccountIsBlockedException extends AppBaseException {

    public AccountIsBlockedException() {
        super(HttpStatus.CONFLICT, "Account is already blocked");
    }

    public AccountIsBlockedException(Throwable cause) {
        super(HttpStatus.CONFLICT, "Account is already blocked", cause);
    }
}
