package pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages;

public record AuthCodeEmailRequest(String toAddress, String langCode, String authCode) {}
