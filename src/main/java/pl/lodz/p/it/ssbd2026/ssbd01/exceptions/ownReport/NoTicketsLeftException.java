package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

/**
 * Wyjątek rzucany w przypadku gdy student, który chce przeprowadzić skan swojego repozytorium nie ma już dostępnych skanowań.
 * Zwraca status 403 not found.
 */
public class NoTicketsLeftException extends AppBaseException {
    public NoTicketsLeftException() {
        super(HttpStatus.FORBIDDEN, "error.repository.no.tickets");
    }

    public NoTicketsLeftException(Throwable cause) {
        super(HttpStatus.FORBIDDEN, "error.repository.no.tickets", cause);
    }
}
