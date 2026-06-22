package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends AppBaseException {

    public EmailSendingException(String messageKey) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, messageKey);
    }

    public EmailSendingException(String messageKey, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, messageKey, cause);
    }
}