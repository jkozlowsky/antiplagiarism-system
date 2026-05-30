package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

public record TeacherSearchDTO(
        String login,
        String firstName,
        String lastName,
        String email
) {}