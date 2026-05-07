package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "{validation.login.notblank}") String login,
        @NotBlank(message = "{validation.password.notblank}") String password
) {}