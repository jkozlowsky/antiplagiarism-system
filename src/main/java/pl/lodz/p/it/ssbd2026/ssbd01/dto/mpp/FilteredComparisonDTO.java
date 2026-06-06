package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilteredComparisonDTO {
    private String matchedWith;
    private Double studentSimilarity;
    private Double otherSimilarity;
    private Double averageSimilarity;
}