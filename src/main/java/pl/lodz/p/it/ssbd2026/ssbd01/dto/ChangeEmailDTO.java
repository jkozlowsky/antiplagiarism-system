package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.*;

public record ChangeEmailDTO(
        @NotBlank(groups = {OnCreate.class})
        @Email(groups = {OnCreate.class})
        @Size(max = AccountConsts.EMAIL_LENGTH, groups = {OnCreate.class})
        String email
) {}
