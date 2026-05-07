package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy konto, które chcemy odblokować nie jest zablokowane
 * Zwraca status 409 Conflict.
 */
public class AccountIsNotBlockedException extends AppBaseException {

    public AccountIsNotBlockedException() {
        super(HttpStatus.CONFLICT, "error.account.not.blocked");
    }

    public AccountIsNotBlockedException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.account.not.blocked", cause);
    }
}
