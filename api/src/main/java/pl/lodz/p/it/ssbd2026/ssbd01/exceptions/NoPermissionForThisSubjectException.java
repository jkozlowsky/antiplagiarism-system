package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class NoPermissionForThisSubjectException extends AppBaseException {
    public NoPermissionForThisSubjectException() {
        super(HttpStatus.FORBIDDEN, "subject.assignment.no.permission");
    }
}
