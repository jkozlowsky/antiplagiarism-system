package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleProjectionListDTO(
        UUID id,
        LocalDateTime scheduleDateTime,
        String tag,
        Integer version,
        Boolean isExecuted,
        Boolean isHasFailed
) {
}