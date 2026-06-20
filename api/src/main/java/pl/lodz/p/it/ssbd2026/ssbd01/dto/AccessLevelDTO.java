package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;

import java.util.UUID;

@Data
public class AccessLevelDTO {

    @Null(groups = {OnCreate.class, OnUpdate.class})
    private UUID id;

    @Null(groups = {OnCreate.class, OnUpdate.class})
    private Integer version;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 3, max = 32, groups = {OnCreate.class, OnUpdate.class})
    private String accessLevelName;

    @Null(groups = {OnCreate.class, OnUpdate.class})
    private Boolean active;
}