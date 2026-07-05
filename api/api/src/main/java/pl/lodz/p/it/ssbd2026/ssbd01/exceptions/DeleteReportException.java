package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class DeleteReportException extends AppBaseException{

    public DeleteReportException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.report.delete", cause);
    }
}
