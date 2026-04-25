package pl.lodz.p.it.ssbd2026.ssbd01.rest.handlers;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Takie DTO do przesyłania ustrukturyzowanych
 * informacji o błędach walidacji w żądaniach.
 */
public class ValidationErrorResponse {

    /**
     * Klasa wewnętrzna reprezentująca pojedyncze naruszenie walidacji dla konkretnego pola.
     */
    @Data
    public static class Violation {
        private final String name;
        private final String message;
    }

    /** Lista wszystkich naruszeń wykrytych podczas pojedynczego żądania. */
    @Getter
    private List<Violation> violations = new ArrayList<>();

    /**
     * Pomocnicza metoda dodająca nowe naruszenie do listy błędów.
     *
     * @param name Nazwa konkretnego pola, w którym wystąpił błąd (np. "email").
     * @param message Treść komunikatu o błędzie (np. "musi być poprawnym adresem e-mail" albo to, co akurat damy).
     */
    public void addViolation(String name, String message) {
        violations.add(new Violation(name, message));
    }
}
