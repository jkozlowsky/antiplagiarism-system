package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.util.UUID;

public record StudentRepositoryDTO(
        UUID repositoryId,
        String repositoryName,
        String subjectName,
        int usedScans,
        int maxScans
) {
}