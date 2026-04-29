package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends AppBaseException {

    public AccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Account not found");
    }

    public AccountNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "Account not found", cause);
    }
}
