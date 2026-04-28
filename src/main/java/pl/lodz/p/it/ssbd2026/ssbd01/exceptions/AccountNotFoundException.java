package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
