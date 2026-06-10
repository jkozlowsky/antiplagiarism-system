package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class ScheduleModificationDeadlineExceededException extends AppBaseException {
    public ScheduleModificationDeadlineExceededException() {
        super(HttpStatus.CONFLICT, "error.schedule.modification.deadline.exceeded");
    }
}