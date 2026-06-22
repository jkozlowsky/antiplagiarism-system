package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class PasswordSameAsOldException extends AppBaseException {

    public PasswordSameAsOldException(){
        super(HttpStatus.CONFLICT, "error.password.same.as.old");
    }
}
