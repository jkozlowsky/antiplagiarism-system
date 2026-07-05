package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.util.List;

public record TeacherMatchedFileDTO(
        String fileA,
        String fileB,
        String codeA,
        String codeB,
        List<Integer> matchedLinesA,
        List<Integer> matchedLinesB,
        int tokens
) {}