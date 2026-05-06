package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token nie może być pusty") String refreshToken
) {}