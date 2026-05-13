package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

public record VerifyEmailSignInRequestDTO (
        @NotBlank(groups = {OnCreate.class})
        @Pattern(regexp = "^[A-Za-z0-9]{10}$")
        String token,
        @NotBlank(groups = {OnCreate.class})
        @Email(groups = {OnCreate.class})
        String email
) {}