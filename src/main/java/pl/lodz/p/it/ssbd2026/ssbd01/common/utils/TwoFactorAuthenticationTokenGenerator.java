package pl.lodz.p.it.ssbd2026.ssbd01.common.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;


@Component
public class TwoFactorAuthenticationTokenGenerator {
    public String generate2FAToken() {
        final SecureRandom random = new SecureRandom();

        int code = 10_000_000 + random.nextInt(90_000_000);
        return String.valueOf(code);
    }
}