package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeacherComparisonDTO(
        String firstSubmission,
        String secondSubmission,
        Double maxSimilarity,
        Double averageSimilarity,
        Double longestMatch,
        String fileA,
        String fileB,
        String codeA,
        String codeB,
        List<Integer> matchedLinesA,
        List<Integer> matchedLinesB,
        List<TeacherMatchedFileDTO> matchedFiles
) {}