package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.util.UUID;

public record SubjectListProjectionDTO(
        UUID id,
        Integer version,
        String name,
        String edition,
        Boolean archived
) {
}