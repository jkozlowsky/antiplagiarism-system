package pl.lodz.p.it.ssbd2026.ssbd01.dto.subject;

import jakarta.validation.constraints.NotBlank;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

public record ChangeManagerRequestDTO(@NotBlank(groups = OnCreate.class) String newManagerLogin) {}
