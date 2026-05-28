package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.SubjectService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RequestMapping("/subjects")
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
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'ADMIN')")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SubjectDTO> getSubjectDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(subjectService.getSubjectDetails(id));
    }

    @Operation(summary = "Utwórz nowy przedmiot (MPP.7)", description = "Tworzy nowy przedmiot i przypisuje do niego aktualnie zalogowanego prowadzącego.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Przedmiot został poprawnie utworzony"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Brak odpowiednich uprawnień", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Użytkownik nie posiada aktywnego poziomu dostępu Nauczyciel", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubject(@Validated(OnCreate.class) @RequestBody SubjectDTO subjectDto, Principal principal) {
        String userLogin = principal.getName();
        subjectService.createSubject(subjectDto, userLogin);
    }

    @PutMapping("/{subjectId}")
    public void updateSubject(@PathVariable UUID subjectId, @RequestBody Object subjectDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(summary = "Usuń przedmiot (MPP.9)", description = "Trwale usuwa przedmiot. Opcjonalnie usuwa również powiązaną organizację na serwerze Gitea.")
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
            @RequestParam(required = false, defaultValue = "false") boolean deleteGiteaOrg,
            Principal principal) {

        subjectService.deleteSubject(subjectId, ifMatch, principal.getName(), deleteGiteaOrg);
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