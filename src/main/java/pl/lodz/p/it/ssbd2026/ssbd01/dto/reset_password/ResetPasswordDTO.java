package pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDTO(@NotBlank String email){
}
