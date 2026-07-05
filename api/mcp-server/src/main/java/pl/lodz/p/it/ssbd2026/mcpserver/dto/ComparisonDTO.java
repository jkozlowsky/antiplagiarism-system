package pl.lodz.p.it.ssbd2026.mcpserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComparisonDTO(
        String firstSubmission,
        String secondSubmission,
        Double maxSimilarity,
        Double averageSimilarity,
        Double longestMatch
) {
}
