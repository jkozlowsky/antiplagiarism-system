package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import java.util.List;

public record AccountWithAccessLevelsDTO(
        AccountDTO account,
        List<AccessLevelDTO> accessLevels
) {}