package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy użytkownik poda błędny login lub hasło do swojego konta
 * Zwraca status 401 Unauthorized.
 */
public class InvalidCredentialsProvidedException extends AppBaseException {

    public InvalidCredentialsProvidedException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid login or password provided");
    }

    public InvalidCredentialsProvidedException(Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, "Invalid login or password provided");
    }
}
