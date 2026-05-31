package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.TeacherSubjectRole;

public record TeacherAssignmentDTO (

        @NotBlank(message = "error.teacher.assignment.login.empty")
        @Size(min = 3, max = AccountConsts.LOGIN_LENGTH, message = "error.teacher.assignment.login.size")
        String login,

        @NotNull(message = "error.teacher.assignment.role.null")
        TeacherSubjectRole role
){
}
