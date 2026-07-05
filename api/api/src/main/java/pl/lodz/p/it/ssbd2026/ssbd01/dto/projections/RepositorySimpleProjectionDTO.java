package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.util.UUID;

public record RepositorySimpleProjectionDTO(
        UUID id,
        String repositoryName,
        Long ticketCount
) {
}