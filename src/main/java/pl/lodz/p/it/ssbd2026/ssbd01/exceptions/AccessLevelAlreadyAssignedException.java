package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class AccessLevelAlreadyAssignedException extends AppBaseException {

    public AccessLevelAlreadyAssignedException() {
        super(HttpStatus.CONFLICT, "Account already possesses this access level");
    }

    public AccessLevelAlreadyAssignedException(Throwable cause) {
        super(HttpStatus.CONFLICT, "Account already possesses this access level", cause);
    }
}