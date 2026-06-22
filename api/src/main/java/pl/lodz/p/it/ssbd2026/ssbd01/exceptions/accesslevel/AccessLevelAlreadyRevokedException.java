package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class AccessLevelAlreadyRevokedException extends AppBaseException {

    public AccessLevelAlreadyRevokedException() {
        super(HttpStatus.CONFLICT, "error.accesslevel.already.revoked");
    }

    public AccessLevelAlreadyRevokedException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.accesslevel.already.revoked", cause);
    }
}
