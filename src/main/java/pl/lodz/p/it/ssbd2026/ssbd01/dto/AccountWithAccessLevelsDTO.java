package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;

import java.util.List;

public record AccountWithAccessLevelsDTO(

        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        @Valid
        AccountDTO account,

        @Valid
        List<AccessLevelDTO> accessLevels
) {}