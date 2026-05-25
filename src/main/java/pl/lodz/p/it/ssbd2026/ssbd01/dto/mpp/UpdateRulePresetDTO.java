package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRulePresetDTO(
        @NotBlank String raportLevelName,

        @NotNull
        @Min(0)
        Integer studentTicketCount,

        @NotNull
        @Min(0)
        Integer minimumTokensMatch,

        @NotNull
        Boolean enableNormalization
) {
}