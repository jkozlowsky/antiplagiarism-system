package pl.lodz.p.it.ssbd2026.ssbd01.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;

public class SubjectCreationValidator implements ConstraintValidator<ValidSubjectCreation, SubjectDTO> {

    @Override
    public boolean isValid(SubjectDTO dto, ConstraintValidatorContext context) {
        if (dto.templateId() != null) {
            return true;
        }

        if (dto.manualRules() != null) {
            return dto.manualRules().raportLevelName() != null
                    && dto.manualRules().studentTicketCount() >= 0
                    && dto.manualRules().minimumTokensMatch() >= 0
                    && dto.manualRules().enableNormalization() != null;
        }

        return false;
    }
}