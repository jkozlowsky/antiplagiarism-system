package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record ResetPasswordEmailRequest(String toAddress, String token, String langCode, String frontendUrl) {}
