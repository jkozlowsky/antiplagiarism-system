package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record RevertEmailRequest(String toAddress, String langCode, String login, String newEmail, String url) {}