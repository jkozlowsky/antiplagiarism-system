package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountListProjectionDTO(
        UUID id,
        String login,
        String name,
        String surname,
        String email,
        LocalDateTime lastLoginSuccessDateTime,
        Integer version
) {
}