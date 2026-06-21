package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record ActivationEmailRequest(String toAddress, String token, String langCode, String frontendUrl) {}