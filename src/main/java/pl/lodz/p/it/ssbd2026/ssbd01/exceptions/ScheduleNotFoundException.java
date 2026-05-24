package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class ScheduleNotFoundException extends AppBaseException {
    public ScheduleNotFoundException() {
        super(HttpStatus.NOT_FOUND, "schedule.not.found.exception.message");
    }
}
