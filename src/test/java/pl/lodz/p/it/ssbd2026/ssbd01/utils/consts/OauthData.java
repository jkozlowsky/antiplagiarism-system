package pl.lodz.p.it.ssbd2026.ssbd01.utils.consts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record OauthData(
        String url,
        String username,
        String password,
        String clientId,
        String clientSecret
) {
}
