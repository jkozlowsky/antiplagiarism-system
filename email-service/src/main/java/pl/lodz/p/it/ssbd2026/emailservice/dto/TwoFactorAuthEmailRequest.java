package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record TwoFactorAuthEmailRequest(String toAddress, String auth2F, String langCode) {}
