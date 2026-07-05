package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateSubjectTemplateDTO(
        @NotBlank @Size(max = 32) String name,
        @NotBlank @Size(max = 32) String organizationName,
        @NotBlank @Size(max = 16) String edition,
        @Size(max = 255) String subjectDescription,
        @NotNull UUID templateId,
        @NotBlank String raportLevelName,
        List<TeacherAssignmentDTO> teachers
) {}