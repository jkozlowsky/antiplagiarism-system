package pl.lodz.p.it.ssbd2026.ssbd01.dto;

import java.util.UUID;

public record AccountDTO(
        UUID id,
        Integer version,
        String login,
        String email,
        String name,
        String surname,
        Boolean isBlocked
) {}