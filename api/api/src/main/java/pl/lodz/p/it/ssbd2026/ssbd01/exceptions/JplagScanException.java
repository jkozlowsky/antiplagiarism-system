package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class JplagScanException extends AppBaseException {
    public JplagScanException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.jplag.scan", cause);
    }
}
