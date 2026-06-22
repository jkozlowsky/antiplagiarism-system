package pl.lodz.p.it.ssbd2026.ssbd01.mpp.reportObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetailedComparison(
        @JsonProperty("submission1") String submission1,
        @JsonProperty("submission2") String submission2,
        @JsonProperty("matches") List<DetailedMatch> matches
) {}