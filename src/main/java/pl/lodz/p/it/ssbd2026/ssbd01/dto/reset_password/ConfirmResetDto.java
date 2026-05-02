package pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmResetDto(
        @NotBlank @NotNull String token,
        @NotBlank @NotNull String newPassword
) {
}
