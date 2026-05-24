package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class GitCloneException extends AppBaseException{
    public GitCloneException(Throwable cause) {
        super(HttpStatus.NOT_FOUND, "error.git.tag.notfound", cause);
    }
}
