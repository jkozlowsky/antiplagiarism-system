package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.util.UUID;

public record AccountRoleProjectionDTO(
        UUID accountId,
        UUID accessLevelId,
        String accessLevelName,
        Boolean active,
        Integer version
) {
}
