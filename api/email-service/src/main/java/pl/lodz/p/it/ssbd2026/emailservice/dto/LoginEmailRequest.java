package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record LoginEmailRequest(String toAddress, String langCode, String login) {}
