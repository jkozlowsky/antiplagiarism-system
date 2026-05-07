package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy użytkownik poda błędny login lub hasło do swojego konta
 * Zwraca status 202 ACCEPTED, żeby nie zdradzać czy dane konto istnieje
 */
public class InvalidCredentialsProvidedException extends AppBaseException {

    public InvalidCredentialsProvidedException() {
        super(HttpStatus.ACCEPTED, "error.invalid.credentials");
    }

    public InvalidCredentialsProvidedException(Throwable cause) {
        super(HttpStatus.ACCEPTED, "error.invalid.credentials");
    }
}
