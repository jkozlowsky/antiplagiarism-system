package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Verify2FARequestDTO(
    @NotBlank
    String login,

    @NotBlank
    @Pattern(regexp = "^\\d{8}$")
    String auth2F

){}
