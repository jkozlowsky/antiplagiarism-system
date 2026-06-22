package pl.lodz.p.it.ssbd2026.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportDetailsDTO(
        UUID id,
        String tag,
        String subjectName,
        LocalDateTime createdAt,
        Double averageSimilarity,
        List<ComparisonDTO> comparisons
) {
}
