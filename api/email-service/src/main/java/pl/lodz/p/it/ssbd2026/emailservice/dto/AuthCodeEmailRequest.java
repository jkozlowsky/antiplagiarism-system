package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record AuthCodeEmailRequest(String toAddress, String langCode, String authCode) {}
