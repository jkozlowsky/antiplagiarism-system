package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetailedMatch(
        @JsonProperty("match1") MatchFragment match1,
        @JsonProperty("match2") MatchFragment match2,
        @JsonProperty("file1") String file1,
        @JsonProperty("file2") String file2,
        @JsonProperty("start1") Integer start1,
        @JsonProperty("length1") Integer length1,
        @JsonProperty("start2") Integer start2,
        @JsonProperty("length2") Integer length2
) {}