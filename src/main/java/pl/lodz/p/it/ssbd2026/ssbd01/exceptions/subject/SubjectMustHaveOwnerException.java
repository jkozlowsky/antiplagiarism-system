package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class SubjectMustHaveOwnerException extends AppBaseException {

    public SubjectMustHaveOwnerException() {super(HttpStatus.BAD_REQUEST, "error.subject.must.have.owner");}
}