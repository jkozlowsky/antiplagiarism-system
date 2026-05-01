package pl.lodz.p.it.ssbd2026.ssbd01.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;

public class PasswordsMatchValidator implements ConstraintValidator <PasswordsMatch, ChangeOwnPasswordDto> {


    @Override
    public void initialize(PasswordsMatch constraintAnnotation){

    }

    @Override
    public boolean isValid(ChangeOwnPasswordDto dto, ConstraintValidatorContext context){

        if (dto.getNewPassword() == null || dto.getConfirmNewPassword() == null)
            return true;
        return dto.getNewPassword().equals(dto.getConfirmNewPassword());
    }
}
