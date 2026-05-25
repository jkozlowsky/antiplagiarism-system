package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Similarities(
        @JsonProperty("AVG") double avg,
        @JsonProperty("MAX") double max,
        @JsonProperty("MAXIMUM_LENGTH") double maximumLength,
        @JsonProperty("LONGEST_MATCH") double longestMatch
) {}
