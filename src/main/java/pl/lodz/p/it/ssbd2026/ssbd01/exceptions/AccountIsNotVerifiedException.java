package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class AccountIsNotVerifiedException extends AppBaseException {
    public AccountIsNotVerifiedException() {
        super(HttpStatus.CONFLICT, "Account is not verified");
    }

    public AccountIsNotVerifiedException(Throwable cause) {
        super(HttpStatus.CONFLICT, "Account is not verified", cause);
    }
}
