package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class Resend2FATokenException extends AppBaseException {
    public Resend2FATokenException() {
        super(HttpStatus.CONFLICT, "Resend 2FA Token Time Exceeded");
    }

    public Resend2FATokenException(Throwable cause) {
        super(HttpStatus.CONFLICT, "Resend 2FA Token Time Exceeded", cause);
    }
}
