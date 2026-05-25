package pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp;

import java.util.UUID;

public record RulePresetDTO(
        UUID id,
        String raportLevelName,
        Integer studentTicketCount
) {}