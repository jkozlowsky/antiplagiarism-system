package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.ownReport;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

/**
 * Wyjątek rzucany w przypadku gdy repozytorium, którego poszukujemy nie istnieje.
 * Zwraca status 404 not found.
 */
public class RepositoryNotFoundException extends AppBaseException {
    public RepositoryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "error.repository.not.found");
    }

    public RepositoryNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "error.repository.not.found", cause);
    }
}


