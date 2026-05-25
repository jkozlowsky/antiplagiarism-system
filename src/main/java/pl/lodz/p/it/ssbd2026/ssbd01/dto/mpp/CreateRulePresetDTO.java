package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRulePresetDTO(
        @Min(0) @NotNull Integer studentTicketCount,
        @NotBlank String raportLevelName
) {}