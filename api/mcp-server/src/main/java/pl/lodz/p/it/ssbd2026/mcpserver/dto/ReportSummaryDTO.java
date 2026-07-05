package pl.lodz.p.it.ssbd2026.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportSummaryDTO(
        UUID id,
        String tag,
        Double average_similarity,
        LocalDateTime created_at,
        String created_by,
        String subject_name,
        Long scanned_repositories
) {
}
