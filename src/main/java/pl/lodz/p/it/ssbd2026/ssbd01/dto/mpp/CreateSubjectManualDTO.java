package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateSubjectManualDTO(
        @NotBlank @Size(max = 32) String name,
        @NotBlank @Size(max = 32) String organizationName,
        @NotBlank @Size(max = 16) String edition,
        @Size(max = 255) String subjectDescription,
        @NotBlank @Size(max = 255) String giteaURL,
        @NotNull RulePresetDTO manualRules,
        List<TeacherAssignmentDTO> teachers
) {}