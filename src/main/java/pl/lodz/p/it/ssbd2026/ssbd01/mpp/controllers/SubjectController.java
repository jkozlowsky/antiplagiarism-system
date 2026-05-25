package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectService;

import java.security.Principal;
import java.util.UUID;

@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@MethodCallLogged
@RestController
public class SubjectController  {
    private final SubjectService subjectService;

    @PostMapping("/{subjectId}/sync")
    public void syncSubjectWithGitea(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    public void getAllSubjects() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{subjectId}")
    public void getSubjectDetails(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping
    public void createSubject(@RequestBody Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{subjectId}")
    public void updateSubject(@PathVariable UUID subjectId, @RequestBody Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(summary = "Usuń przedmiot (MPP.9)", description = "Trwale usuwa przedmiot, jego dane skojarzone z bazy oraz skojarzone pliki raportów z dysku.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Przedmiot został pomyślnie usunięty"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień do usunięcia przedmiotu (nie jesteś głównym prowadzącym)"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono podanego przedmiotu"),
            @ApiResponse(responseCode = "409", description = "Dane uległy zmianie (Błąd blokady optymistycznej) - odśwież widok")
    })
    @DeleteMapping("/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSubject(
            @PathVariable UUID subjectId,
            @RequestHeader("If-Match") String ifMatch,
            Principal principal) {

        subjectService.deleteSubject(subjectId, ifMatch, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{subjectId}/aggregated-values")
    public void calculateAggregatedValues(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PatchMapping("/{subjectId}/manager")
    public void changeSubjectManager(@PathVariable UUID subjectId, @RequestBody UUID newManagerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}