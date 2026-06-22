package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportDTO(
    UUID id,
    String tag,
    Double average_similarity,
    LocalDateTime created_at,
    String created_by,
    String subject_name,
    Long  scanned_repositories
) {
}
