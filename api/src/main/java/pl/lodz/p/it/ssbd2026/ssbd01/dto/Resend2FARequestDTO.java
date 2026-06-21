package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;

public record Resend2FARequestDTO(
        @NotBlank
        String login
) {
}
