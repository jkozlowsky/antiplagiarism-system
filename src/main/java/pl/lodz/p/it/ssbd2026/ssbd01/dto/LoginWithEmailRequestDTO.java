package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;

public record LoginWithEmailRequestDTO(
        @NotBlank(groups = {OnCreate.class}) @Email(groups = {OnCreate.class}) String email
) {}