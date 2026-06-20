package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Powinno się nigdy nie przydać bo wyrzucony tylko w przypadku jeżeli nie istnieje algorytmu SHA-256,
 * albo ktoś podmienił stałą w OnetimeTokenConsts.
 */
public class HashEncryptionException extends RuntimeException{
    public HashEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
