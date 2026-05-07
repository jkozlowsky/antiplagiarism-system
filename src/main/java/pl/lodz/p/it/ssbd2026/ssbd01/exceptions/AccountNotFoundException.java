package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountNotFoundException extends AppBaseException {

    public AccountNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public AccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.account.not.found");
    }

    protected AccountNotFoundException(String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }

    public AccountNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "error.account.not.found", cause);
    }
}
