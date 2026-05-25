package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class RulePresetNotFoundException extends AppBaseException {
    public RulePresetNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.rule.preset.not.found");
    }
}
