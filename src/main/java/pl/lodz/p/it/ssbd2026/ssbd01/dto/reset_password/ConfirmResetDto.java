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
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Hasło musi zawierać co najmniej jedną małą literę, jedną dużą literę i jedną cyfrę",
                groups = OnCreate.class
        )
        String newPassword
) {
}
