package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class SubjectAssignmentNotFoundException extends AppBaseException {
    public SubjectAssignmentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.subject.assignment.not.found");
    }
}