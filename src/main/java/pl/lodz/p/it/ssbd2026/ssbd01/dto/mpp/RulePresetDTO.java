package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RulePresetDTO(
        @NotBlank UUID id,
        @NotBlank String raportLevelName,
        @NotNull @Min(0) Integer studentTicketCount,
        @NotNull @Min(1) Integer minimumTokensMatch,
        @NotBlank Boolean enableNormalization,
//        @NotNull Integer version,
        String versionHash
) {
}