package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany w przypadku wystąpienia konfliktu unikalności danych w bazie danych.
 * Zwraca status 409 Conflict.
 */
public class AccountConstraintViolationException extends AppBaseException {

    public AccountConstraintViolationException(String key) {
        super(HttpStatus.CONFLICT, key);
    }

    public AccountConstraintViolationException() {
        super(HttpStatus.CONFLICT, "error.account.constraint.violation");
    }

    public AccountConstraintViolationException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.account.constraint.violation", cause);
    }
}
