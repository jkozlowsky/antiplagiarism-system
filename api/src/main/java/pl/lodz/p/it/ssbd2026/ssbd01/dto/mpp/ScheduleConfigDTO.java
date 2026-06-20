package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.util.UUID;

public record ScheduleConfigDTO(
        UUID scheduleId,
        UUID subjectId,
        String organizationName,
        String giteaTag,
        Integer minimumTokensMatch,
        Boolean enableNormalization
) {}
