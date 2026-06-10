package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class OriginalLanguageNotFoundException extends AppBaseException {
    public OriginalLanguageNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.original.language.notfound");
    }
}
