package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public abstract class AppBaseException extends ResponseStatusException {
    protected AppBaseException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    protected AppBaseException(HttpStatusCode status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
