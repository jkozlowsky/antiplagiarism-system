package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

/**
 * Powinno się nigdy nie przydać bo wyrzucony tylko w przypadku jeżeli nie istnieje algorytmu SHA-256,
 * albo ktoś podmienił stałą w OnetimeTokenConsts.
 */
public class HashEncryptionException extends RuntimeException{
    public HashEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
