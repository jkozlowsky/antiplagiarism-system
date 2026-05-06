package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Login nie może być pusty") String login,
        @NotBlank(message = "Hasło nie może być puste") String password
) {}