package pl.lodz.p.it.ssbd2026.ssbd01.exceptions.accesslevel;

import org.springframework.http.HttpStatus;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

public class AccessLevelNotAssignedException extends AppBaseException {

  public AccessLevelNotAssignedException() {
    super(HttpStatus.CONFLICT, "error.accesslevel.not.assigned");
  }

  public AccessLevelNotAssignedException(Throwable cause) {
    super(HttpStatus.CONFLICT, "error.accesslevel.not.assigned", cause);
  }
}