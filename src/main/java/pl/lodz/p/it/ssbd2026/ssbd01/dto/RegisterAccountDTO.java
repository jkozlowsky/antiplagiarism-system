package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

@Data
public class RegisterAccountDTO {
    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    private String firstName;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    private String lastName;

    @NotBlank(groups = OnCreate.class)
    @Email(groups = OnCreate.class)
    private String email;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = OnCreate.class, message = "Login może zawierać tylko litery i cyfry")
    private String login;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 8, groups = OnCreate.class)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", groups = OnCreate.class)
    private String password;

    @NotBlank(groups = OnCreate.class)
    private String confirmPassword;
}
