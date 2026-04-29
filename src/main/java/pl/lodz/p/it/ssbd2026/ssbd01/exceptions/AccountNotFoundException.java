package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class AccountNotFoundException extends AppBaseException{
    public AccountNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    protected AccountNotFoundException(HttpStatusCode status, String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }

}
