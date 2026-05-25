package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy konto, użytkownik (prowadzący dodatkowy) nie może edytować harmonogramu z uwagi na brak uprawnień.
 * Zwraca status 403 FORBIDDEN.
 */
public class NoPermissionToEditScheduleException extends AppBaseException {

    public NoPermissionToEditScheduleException() {
        super(HttpStatus.FORBIDDEN, "schedule.cannot.edit");
    }

    public NoPermissionToEditScheduleException(Throwable cause) {
        super(HttpStatus.FORBIDDEN, "schedule.cannot.edit", cause);
    }
}
