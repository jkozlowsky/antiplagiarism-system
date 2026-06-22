package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record RevertEmailRequest(String toAddress, String langCode, String login, String newEmail, String url) {}
