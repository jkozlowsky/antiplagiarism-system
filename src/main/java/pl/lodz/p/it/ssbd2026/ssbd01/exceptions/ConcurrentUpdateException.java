package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany w przypadku wystąpienia konfliktu blokady optymistycznej.
 * Zwraca status 409 Conflict.
 */
public class ConcurrentUpdateException extends AppBaseException {

    public ConcurrentUpdateException() {
        super(HttpStatus.CONFLICT, "The object was modified since read for update (version conflict)");
    }

    public ConcurrentUpdateException(Throwable cause) {
        super(HttpStatus.CONFLICT, "The object was modified since read for update (version conflict)", cause);
    }
}
