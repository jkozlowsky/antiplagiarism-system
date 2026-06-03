package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import pl.lodz.p.it.ssbd2026.ssbd01.dto.repository.RepositoryWithStudentDTO;
import java.util.UUID;

public record StudentSubjectDetailsDTO(
        UUID subjectId,
        String name,
        String organizationName,
        String edition,
        String subjectDescription,
        String giteaURL,
        RepositoryWithStudentDTO studentRepository,
        String reportVisibilityLevel
) {}