package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class RulePresetInvalidPartametersException extends AppBaseException {

    public RulePresetInvalidPartametersException() {
        super(HttpStatus.BAD_REQUEST, "error.rolePreset.invalid.parameters");
    }
}
