package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class OneTimeTokenInvalidException extends AppBaseException {

    public OneTimeTokenInvalidException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }
    public OneTimeTokenInvalidException(HttpStatusCode status, String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}
