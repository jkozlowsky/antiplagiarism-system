package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;
import org.springframework.http.HttpStatus;

public class SubjectAccessDeniedException extends AppBaseException {
    public SubjectAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "error.subject.access.denied");
    }

    public SubjectAccessDeniedException(Throwable cause) {
        super(HttpStatus.FORBIDDEN, "error.subject.access.denied", cause);
    }
}