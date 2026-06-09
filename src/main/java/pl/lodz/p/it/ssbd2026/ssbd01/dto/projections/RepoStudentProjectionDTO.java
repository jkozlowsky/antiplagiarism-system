package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.util.UUID;

public record RepoStudentProjectionDTO (
        UUID repositoryId,
        UUID studentId,
        String login,
        String name,
        String surname
){ }