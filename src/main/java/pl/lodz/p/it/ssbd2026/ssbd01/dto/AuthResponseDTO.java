package pl.lodz.p.it.ssbd2026.ssbd01.dto;

public record AuthResponseDTO(
        String token,
        String refreshToken
) {}