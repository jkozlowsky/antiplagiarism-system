package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class LanguageDictNotFoundException extends AppBaseException {

    public LanguageDictNotFoundException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.language.dict.not.found");
    }

    public LanguageDictNotFoundException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.language.dict.not.found", cause);
    }
}
