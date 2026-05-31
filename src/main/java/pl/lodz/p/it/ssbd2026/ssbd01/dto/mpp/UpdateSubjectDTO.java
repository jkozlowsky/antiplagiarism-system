package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateSubjectDTO(
        @NotBlank(message = "error.subject.update.name.empty")
        @Size(max = 100, message = "error.subject.update.name.size")
        String name,

        @Size(max = 2000, message = "error.subject.update.description.size")
        String subjectDescription,

        @Size(max = 255, message = "error.subject.update.giteaURL.size")
        String giteaURL,

        @NotNull(message = "error.subject.update.teachers.null")
        List<@Valid TeacherAssignmentDTO> teachers,

        @Valid
        UpdateRulePresetDTO rules
) {}