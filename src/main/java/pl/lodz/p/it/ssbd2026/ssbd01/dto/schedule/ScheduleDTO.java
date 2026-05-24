package pl.lodz.p.it.ssbd2026.ssbd01.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleDTO(
        @NotNull(groups = OnCreate.class)
        UUID id,
        @NotNull(groups = OnCreate.class)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduleDateTime,
        @NotNull(groups = OnCreate.class)
        @NotBlank(groups = {OnCreate.class, OnUpdate.class})
        String tag
) {}
