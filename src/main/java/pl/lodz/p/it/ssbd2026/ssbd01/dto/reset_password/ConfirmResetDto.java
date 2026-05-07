package pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

public record ConfirmResetDto(
        @NotBlank(groups = OnCreate.class)
        @NotNull(groups = OnCreate.class)
        String token,

        @NotBlank(groups = OnCreate.class)
        @NotNull(groups = OnCreate.class)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
                message = "validation.password.complex",
                groups = OnCreate.class
        )
        String newPassword
) {
}
