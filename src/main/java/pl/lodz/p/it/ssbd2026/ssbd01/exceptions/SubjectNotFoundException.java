package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class SubjectNotFoundException extends AppBaseException {
    public SubjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.subject.not.found");
    }
}
