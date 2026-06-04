package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchFragment(
        @JsonProperty("file") String file,
        @JsonProperty("start") Integer start,
        @JsonProperty("length") Integer length
) {}