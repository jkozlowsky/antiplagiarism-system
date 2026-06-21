package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record OwnEmailChangeDTO(
        @NotBlank
        String token,

        @NotBlank
        String password,

        @NotBlank
        @Email
        String newEmail
) {}
