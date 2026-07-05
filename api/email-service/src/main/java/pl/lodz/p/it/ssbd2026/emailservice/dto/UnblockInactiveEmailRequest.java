package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record UnblockInactiveEmailRequest(String toAddress, String langCode, String login, String token, String frontendUrl) {}
