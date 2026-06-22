package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record AdminLoginEmailRequest(String toAddress, String langCode, String login, String ipAddress) {}
