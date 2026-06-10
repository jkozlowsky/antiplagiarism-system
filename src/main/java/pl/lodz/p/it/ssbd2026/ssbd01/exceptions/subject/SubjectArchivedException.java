package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.subject;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class SubjectArchivedException extends AppBaseException {
    public SubjectArchivedException() {
        super(HttpStatus.CONFLICT, "error.subject.archived");
    }
}