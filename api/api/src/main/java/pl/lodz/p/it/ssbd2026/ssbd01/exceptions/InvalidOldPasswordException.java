package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOldPasswordException extends AppBaseException {

    public InvalidOldPasswordException() {
        super(HttpStatus.BAD_REQUEST, "error.password.invalid.old");
    }

}
