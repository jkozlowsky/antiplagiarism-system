package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.http.HttpStatus;

public class JGitException extends AppBaseException{
    public JGitException(JGitInternalException e) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.jgit.problem", e);
    }
}
