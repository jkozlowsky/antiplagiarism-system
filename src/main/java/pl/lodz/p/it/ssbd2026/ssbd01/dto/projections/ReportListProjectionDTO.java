package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportListProjectionDTO(
        UUID id,
        String tag,
        Double averageSimilarity,
        String subjectName,
        LocalDateTime creationDate,
        Long scannedRepositories
) {
}
