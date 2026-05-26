package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ReportNotFoundException extends AppBaseException {

    public ReportNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.report.notfound");
    }
}
