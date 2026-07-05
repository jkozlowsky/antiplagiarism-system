package pl.lodz.p.it.ssbd2026.emailservice.dto;

public record UrlEmailRequest(String toAddress, String langCode, String login, String url) {}
