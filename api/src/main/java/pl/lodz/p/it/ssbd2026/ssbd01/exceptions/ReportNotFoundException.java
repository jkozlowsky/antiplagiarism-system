package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class ReportNotFoundException extends AppBaseException {

    public ReportNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.report.notfound");
    }
}
