package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class AccessLevelDictNotFoundException extends AppBaseException {

    public AccessLevelDictNotFoundException() {
        super(HttpStatus.CONFLICT, "Invalid access level name provided");
    }

    public AccessLevelDictNotFoundException(Throwable cause) {
        super(HttpStatus.CONFLICT, "Invalid access level name provided", cause);
    }
}