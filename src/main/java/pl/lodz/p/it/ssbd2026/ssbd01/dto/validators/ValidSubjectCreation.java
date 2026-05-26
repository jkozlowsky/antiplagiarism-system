package pl.lodz.p.it.ssbd2026.ssbd01.dto.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SubjectCreationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSubjectCreation {
    String message() default "error.subject.creation.invalid.rules";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}