package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;
import org.springframework.http.HttpStatus;

public class SubjectNotFoundException extends AppBaseException {
    public SubjectNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.subject.not.found");
    }

    public SubjectNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "error.subject.not.found", cause);
    }
}