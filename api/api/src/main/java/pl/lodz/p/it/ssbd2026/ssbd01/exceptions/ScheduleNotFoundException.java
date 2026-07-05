package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Wyjątek rzucany kiedy konto, nie możemy znaleźć harmonogramu, który chcemy edytować.
 * Zwraca status 404 NOT FOUND.
 */
public class ScheduleNotFoundException extends AppBaseException {
    public ScheduleNotFoundException() {
        super(HttpStatus.NOT_FOUND, "schedule.not.found.message");
    }

    public ScheduleNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "schedule.not.found.message", cause);
    }
}
