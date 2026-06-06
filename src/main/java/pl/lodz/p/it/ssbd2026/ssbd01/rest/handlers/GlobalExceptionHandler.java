package pl.lodz.p.it.ssbd2026.ssbd01.rest.handlers;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pl.lodz.p.it.ssbd2026.ssbd01.config.i18n.Translator;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.AppBaseException;

/**
 * Globalna klasa obsługująca wyjątki rzucane w całej aplikacji.
 * Adnotacja @RestControllerAdvice działa tak, że każda metoda w tej
 * klasie wie, żeby zwracać dane bezpośrednio do ciała odpowiedzi HTTP.
 * Przez to adnotacja @ResponseBody nie jest potrzebna.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final Translator translator;
    /**
     * Obsługuje działanie wyjątków bazowych aplikacji (AppBaseException).
     * Zwraca bezpośrednio ResponseEntity, co jest dobrą praktyką, ponieważ
     * eliminuje wewnętrzne przekierowania do /error i unika problemów ze Spring Security (403 Forbidden).
     *
     * @param exception Przechwycony wyjątek aplikacyjny.
     * @return Odpowiedź HTTP z odpowiednim statusem i komunikatem z wyjątku.
     */
    @ExceptionHandler(AppBaseException.class)
    public ResponseEntity<String> handleAppBaseException(AppBaseException exception) {
        String translatedMessage = translator.translate(exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(translatedMessage);
    }

    /**
     * Obsługuje błędy odczytu wiadomości HTTP.
     * Używany, gdy ciało żądania (JSON) jest niepoprawne składniowo, nieczytelne
     * lub nie może zostać zdeserializowane na obiekt Java u nas w aplikacji.
     * Zwraca status 400 Bad Request.
     *
     * @param hmnre Wyjątek nieczytelnej wiadomości HTTP.
     * NIC nie zwraca, bo te wyjątki zwracane przez bibliotekę Jackson są nieczytelne.
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleMessageNotReadableException(HttpMessageNotReadableException hmnre) {
    }

    /**
     * Obsługuje błędy systemowe JPA/Hibernate.
     * Używany w sytuacjach krytycznych po stronie bazy danych lub warstwy trwałości,
     * takich jak przekroczenie czasu transakcji lub problemy z połączeniem z bazą.
     * Zwraca status 503 Service Unavailable.
     *
     * @param jse Wyjątek systemowy JPA.
     * @return Odpowiedź ze statusem 503 i odpowiednim komunikatem o błędzie.
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<String> handleJpaSystemException(JpaSystemException jse) {
        log.error("A critical database error occurred: ", jse);
        String translatedMessage = translator.translate("error.database");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(translatedMessage);
    }

    /**
     * Obsługuje Bean Validation.
     * Używany, gdy walidacja zawiedzie na poziomie parametrów metod (np. @NotNull przy @PathVariable)
     * lub bezpośrednio na encjach podczas operacji bazodanowych.
     * Mapuje naruszenia na obiekt ValidationErrorResponse (nasza specjalna klasa od tego)
     * ze statusem 400 Bad Request.
     *
     * @param e Wyjątek naruszenia walidacji.
     * @return Zwraca status 400 Bad Request i sformatowany obiekt z listą błędów walidacji.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleConstraintValidationException(ConstraintViolationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();

        e.getConstraintViolations().forEach(violation -> error.getViolations().add(
                new ValidationErrorResponse.Violation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        ));

        return error;
    }

    /**
     * Obsługuje błędy walidacji obiektów DTO przesłanych w @RequestBody.
     * Używany, gdy adnotacja @Valid przy argumencie kontrolera wykryje błędy (np. pusty e-mail).
     * Zwraca szczegółową listę błędnych pól w obiekcie ValidationErrorResponse ze statusem 400.
     *
     * @param e Wyjątek niepoprawnych argumentów metody.
     * @return Zwraca status 400 Bad Request i sformatowany obiekt z listą błędów walidacji.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> error.getViolations().add(
                        new ValidationErrorResponse.Violation(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                )
        );
        return error;
    }

    /**
     * Obsługuje błędy braku uprawnień (Security).
     * Używany, kiedy Spring Security zablokuje dostęp do metody oznaczonej np. @PreAuthorize,
     * ponieważ użytkownik nie posiada wymaganej roli lub uprawnień.
     *
     * @param exception Wyjątek odmowy dostępu.
     * @param request Obiekt żądania webowego.
     * @return Zwraca status 403 Forbidden z komunikatem o braku uprawnień (możemy go dostosować pod siebie).
     */
    @ExceptionHandler({AuthorizationDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<Object> handleAuthorizationException(RuntimeException exception, WebRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + exception.getMessage());
        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("No access: " + exception.getMessage());
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.getViolations().add(
                new ValidationErrorResponse.Violation(
                        e.getName(),
                        "Invalid format for value: " + e.getValue()
                )
        );
        return error;
    }

    /**
     * Rezerwowy handler dla wszystkich nieoczekiwanych wyjątków RuntimeException.
     * Miejmy nadzieję, że nigdy nie będzie potrzebny (na pewno będzie).
     *
     * @param exception Przechwycony nieobsłużony wyjątek czasu wykonania.
     * @param request Obiekt żądania webowego.
     * @return Zwraca status 500 Internal Server Error z opisem błędu.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAllUncaughtException(RuntimeException exception, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception: " + exception.getMessage());
    }
}
