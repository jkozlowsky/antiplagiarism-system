package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class PasswordAlreadyUsedInPastException extends AppBaseException {

    public PasswordAlreadyUsedInPastException() {
        super(HttpStatus.CONFLICT, "error.password.already.used");
    }

    public PasswordAlreadyUsedInPastException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.password.already.used", cause);
    }
}