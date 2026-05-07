package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class AccessLevelDictNotFoundException extends AppBaseException {

    public AccessLevelDictNotFoundException() {
        super(HttpStatus.CONFLICT, "error.accesslevel.dict.not.found");
    }

    public AccessLevelDictNotFoundException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.accesslevel.dict.not.found", cause);
    }
}