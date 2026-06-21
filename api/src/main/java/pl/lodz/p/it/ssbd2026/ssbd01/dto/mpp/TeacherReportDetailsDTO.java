package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TeacherReportDetailsDTO(
        UUID id,
        String tag,
        String subjectName,
        LocalDateTime createdAt,
        Double averageSimilarity,
        List<TeacherComparisonDTO> comparisons
) {}