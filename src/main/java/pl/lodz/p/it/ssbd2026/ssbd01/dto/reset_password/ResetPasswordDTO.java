package pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

public record ResetPasswordDTO(
        @NotBlank(groups = OnCreate.class)
        @Email(groups = OnCreate.class)
        String email
) {
}
