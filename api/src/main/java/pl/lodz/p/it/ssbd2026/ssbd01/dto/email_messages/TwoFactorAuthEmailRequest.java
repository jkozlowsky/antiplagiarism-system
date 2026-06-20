package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record TwoFactorAuthEmailRequest(String toAddress, String auth2F, String langCode) {}
