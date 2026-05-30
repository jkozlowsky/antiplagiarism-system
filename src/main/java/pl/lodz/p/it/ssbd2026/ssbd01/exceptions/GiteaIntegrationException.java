package pl.lodz.p.it.ssbd2026.ssbd01.exceptions;

import org.springframework.http.HttpStatus;

public class GiteaIntegrationException extends AppBaseException {
    public GiteaIntegrationException() {
        super(HttpStatus.BAD_GATEWAY, "error.subject.gitea.integration");
    }

    public GiteaIntegrationException(Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, "error.subject.gitea.integration", cause);
    }
}