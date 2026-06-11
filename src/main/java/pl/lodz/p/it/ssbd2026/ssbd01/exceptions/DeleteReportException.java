package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;

public class DeleteReportException extends AppBaseException{

    public DeleteReportException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.report.delete", cause);
    }
}
