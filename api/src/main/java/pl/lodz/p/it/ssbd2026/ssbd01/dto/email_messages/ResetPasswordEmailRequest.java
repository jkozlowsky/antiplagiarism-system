package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record ResetPasswordEmailRequest(String toAddress, String token, String langCode, String frontendUrl) {}
