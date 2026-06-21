package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record AccessLevelEmailRequest(String toAddress, String langCode, String login, String accessLevelName) {}
