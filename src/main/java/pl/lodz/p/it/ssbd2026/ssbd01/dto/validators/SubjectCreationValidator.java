package pl.lodz.p.it.ssbd2026.ssbd01.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;

public class SubjectCreationValidator implements ConstraintValidator<ValidSubjectCreation, SubjectDTO> {
    @Override
    public boolean isValid(SubjectDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (dto.templateId() != null) {
            return dto.manualRules() == null;
        }
        else {
            return dto.manualRules() != null;
        }
    }
}