package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.raportLevel;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class RaportLevelDictNotFoundException extends AppBaseException {
    public RaportLevelDictNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.reportLevel.dict.not.found");
    }
}
