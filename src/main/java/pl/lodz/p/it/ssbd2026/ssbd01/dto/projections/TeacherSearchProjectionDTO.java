package pl.lodz.p.it.ssbd2026.ssbd01.dto.projections;

import java.util.UUID;

public record TeacherSearchProjectionDTO(
        UUID teacherId,
        String login,
        String email,
        String name,
        String surname
) {
}