package pl.lodz.p.it.ssbd2026.ssbd01.common.utils;

import org.springframework.stereotype.Component;
import com.github.curiousoddman.rgxgen.RgxGen;


@Component
public class TwoFactorAuthenticationTokenGenerator {
    public String generate2FAToken() {
        return RgxGen.parse("[0-9]{8}").generate();
    }
}