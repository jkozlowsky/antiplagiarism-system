package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountDTO(
        @Null(groups = {OnCreate.class, OnUpdate.class})
        UUID id,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        Integer version,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        String versionHash,

        @Null(groups = OnUpdate.class)
        @Size(min = 4, max = 64, groups = OnCreate.class)
        String login,

        @Null(groups = OnUpdate.class)
        @Size(min = 12, groups = OnCreate.class)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "validation.password.pattern",
                groups = OnCreate.class
        )
        String password,


        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean isVerified,
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean isBlocked,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 3, max = 32, groups = {OnCreate.class, OnUpdate.class})
        String name,

        @NotBlank(groups = OnCreate.class)
        @Size(min = 3, max = 32, groups = {OnCreate.class, OnUpdate.class})
        String surname,

        @Null(groups = OnUpdate.class)
        @NotBlank(groups = OnCreate.class)
        @Email(groups = {OnCreate.class})
        @Size(max = 128, groups = {OnCreate.class})
        String email,

        @Null(groups = {OnCreate.class, OnUpdate.class})
        LocalDateTime lastLoginSuccessDateTime
//
//        @Null(groups = {OnCreate.class, OnUpdate.class})
//        LocalDateTime lastLoginFailureDateTime,
//
//        @Null(groups = {OnCreate.class, OnUpdate.class})
//        String lastSuccessfulLoginIp,
//
//        @Null(groups = {OnCreate.class, OnUpdate.class})
//        String lastFailedLoginIp,
//
//        @Null(groups = {OnCreate.class, OnUpdate.class})
//        String language
) {
}