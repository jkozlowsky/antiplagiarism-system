package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;

import java.util.UUID;

public record AccountDTO(
        @Null(groups = {OnCreate.class, OnUpdate.class})
        UUID id,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        Integer version,

        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        String versionHash,

        @Null(groups = OnUpdate.class)
        @Size(min = 4, max = 64, groups = OnCreate.class)
        String login,

        @Null(groups = OnUpdate.class)
        @Size(min = 12, groups = OnCreate.class)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Hasło musi zawierać co najmniej jedną małą literę, jedną dużą literę i jedną cyfrę",
                groups = OnCreate.class
        )
        String password,


        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean verified,
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean active,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 3, max = 32, groups = {OnCreate.class, OnUpdate.class})
        String name,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 3, max = 32, groups = {OnCreate.class, OnUpdate.class})
        String surname,

        @NotBlank(groups = OnCreate.class)
        @Email(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = 128, groups = {OnCreate.class, OnUpdate.class})
        String email
) {
}