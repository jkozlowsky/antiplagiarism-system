package pl.lodz.p.it.ssbd2026.ssbd01.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.validators.PasswordsMatch;

@Data
@NoArgsConstructor
@PasswordsMatch
public class ChangeOtherPasswordDto {

    @Size(min = 12, message = "error.password.too.short")
    @NotBlank(message = "error.new.password.blank")
    private String newPassword;

    @NotBlank(message = "error.confirm.password.blank")
    private String confirmNewPassword;
}
