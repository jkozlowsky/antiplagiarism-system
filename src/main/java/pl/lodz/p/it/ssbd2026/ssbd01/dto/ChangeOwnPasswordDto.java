package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

@Data
@NoArgsConstructor
public class ChangeOwnPasswordDto {

    @NotBlank(message = "error.old.password.blank")
    private String oldPassword;

    @Size(min = 12, message = "error.password.too.short")
    @NotBlank(message = "error.new.password.blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Hasło musi zawierać co najmniej jedną małą literę, jedną dużą literę i jedną cyfrę",
            groups = OnCreate.class
    )
    private String newPassword;


}
