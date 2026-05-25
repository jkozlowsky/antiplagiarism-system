package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.rulePreset;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class RulePresetTemplateNotFoundException extends AppBaseException {

    public RulePresetTemplateNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.rolePreset.template.not.found");
    }
}
