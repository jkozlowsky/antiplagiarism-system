package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

@Data
public class RegisterAccountDTO {
    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[\\p{L}][\\p{L}\\s\\-']+$", groups = OnCreate.class, message = "validation.name.pattern")
    private String name;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[\\p{L}][\\p{L}\\s\\-']+$", groups = OnCreate.class, message = "validation.surname.pattern")
    private String surname;

    @NotBlank(groups = OnCreate.class)
    @Email(groups = OnCreate.class)
    private String email;

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 32, groups = OnCreate.class)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", groups = OnCreate.class, message = "validation.login.pattern")
    private String login;

    @ToString.Exclude
    @NotBlank(groups = OnCreate.class)
    @Size(min = 8, max = 64, groups = OnCreate.class)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
            groups = OnCreate.class,
            message = "validation.password.complex"
    )
    private String password;
}
