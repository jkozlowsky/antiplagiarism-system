package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilteredComparisonDTO(
        String matchedWith,
        Double maxSimilarity,
        Double averageSimilarity,
        Double longestMatch
) {}