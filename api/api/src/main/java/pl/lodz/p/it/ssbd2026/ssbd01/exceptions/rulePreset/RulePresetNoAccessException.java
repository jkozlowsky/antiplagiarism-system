package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class RulePresetNoAccessException extends AppBaseException {
    public RulePresetNoAccessException() {
        super(HttpStatus.BAD_REQUEST, "error.rule.preset.access.denied");
    }
}
