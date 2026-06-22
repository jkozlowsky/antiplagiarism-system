package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class RulePresetNotSetForSubjectException extends AppBaseException {
    public RulePresetNotSetForSubjectException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
