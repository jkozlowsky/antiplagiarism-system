package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record CreateRulePresetDTO(
        @Min(0) Integer studentTicketCount,
        String raportLevelName,
        UUID presetId,
        @Min(1) Integer minimumTokensMatch,
        Boolean enableNormalization
) {
}