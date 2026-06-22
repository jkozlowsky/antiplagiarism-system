package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

/**
 * Wyjątek rzucany w przypadku gdy student, który chce przeprowadzić skan swojego repozytorium nie ma już dostępnych skanowań.
 * Zwraca status 409 Conflict.
 */
public class NoTicketsLeftException extends AppBaseException {
    public NoTicketsLeftException() {
        super(HttpStatus.CONFLICT, "error.repository.no.tickets");
    }

    public NoTicketsLeftException(Throwable cause) {
        super(HttpStatus.CONFLICT, "error.repository.no.tickets", cause);
    }
}
