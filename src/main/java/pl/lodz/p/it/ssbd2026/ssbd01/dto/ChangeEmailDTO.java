package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.*;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

public record ChangeEmailDTO(@NotBlank @Email @Size(max = AccountConsts.EMAIL_LENGTH) String email,
        @NotNull Integer version
) {}
