package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class ReportFileException extends AppBaseException {
    public ReportFileException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.report.file", cause);
    }
}
