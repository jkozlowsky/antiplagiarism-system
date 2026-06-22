package pl.lodz.p.it.ssbd2026.ssbd01.common.utils;

import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;

import java.security.SecureRandom;


@Component
public class AuthenticationTokenValueGenerator {
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String generate2FAToken() {
        final SecureRandom random = new SecureRandom();

        int code = 10_000_000 + random.nextInt(90_000_000);
        return String.valueOf(code);
    }

    public String generateAuthWithEmailOnlyCode() {
        final SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(OneTimeTokenConsts.CODE_LOGIN_TOKEN_LENGTH);

        for (int i = 0; i < OneTimeTokenConsts.CODE_LOGIN_TOKEN_LENGTH; i++) {
            code.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }

        return code.toString();
    }
}