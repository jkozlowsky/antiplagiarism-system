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
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.SubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectManualDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateSubjectTemplateDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherAssignmentDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateSubjectDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.ChangeManagerRequestDTO;
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
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{subjectId}/users")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<List<TeacherAssignmentDTO>> getSubjectUsers(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(subjectService.getSubjectUsers(subjectId));
    }

    @GetMapping("/{subjectId}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<SubjectDTO> getSubjectDetails(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(subjectService.getSubjectDetails(subjectId));
    }


    @Operation(summary = "Utwórz nowy przedmiot (konfiguracja ręczna) (MPP.7)", description = "Tworzy nowy przedmiot z ręczną konfiguracją reguł i przypisuje do niego aktualnie zalogowanego prowadzącego.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Przedmiot został poprawnie utworzony"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Brak odpowiednich uprawnień", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Użytkownik nie posiada aktywnego poziomu dostępu Nauczyciel", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/manual")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubjectManual(@Validated(OnCreate.class) @RequestBody CreateSubjectManualDTO dto, Principal principal) {
        String userLogin = principal.getName();
        subjectService.createSubjectManual(dto, userLogin);
    }

    @Operation(summary = "Utwórz nowy przedmiot (szablon) (MPP.7)", description = "Tworzy nowy przedmiot wykorzystując szablon reguł i przypisuje do niego aktualnie zalogowanego prowadzącego.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Przedmiot został poprawnie utworzony"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Brak odpowiednich uprawnień", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Użytkownik nie posiada aktywnego poziomu dostępu Nauczyciel", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/template")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubjectFromTemplate(@Validated(OnCreate.class) @RequestBody CreateSubjectTemplateDTO dto, Principal principal) {
        String userLogin = principal.getName();
        subjectService.createSubjectFromTemplate(dto, userLogin);
    }


    @PutMapping("/{subjectId}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<Void> updateSubject( @PathVariable UUID subjectId,
                                               @RequestHeader(org.springframework.http.HttpHeaders.IF_MATCH) String ifMatch,
                                               @RequestBody @jakarta.validation.Valid UpdateSubjectDTO dto)
    {
        subjectService.updateSubject(subjectId, dto, ifMatch);
        return ResponseEntity.noContent().build();
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

    @Operation(summary = "Zmień kierownika przedmiotu", description = "Wyznacza nowego kierownika przedmiotu na podstawie jego loginu. Stary kierownik utraci swój poziom uprawnień i zostanie mu przypisane uprawnienie VIEWER.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kierownik przedmiotu został pomyślnie zmieniony"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu lub wskazanego nauczyciela")
    })
    @PatchMapping("/{subjectId}/manager")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public void changeSubjectManager(@PathVariable UUID subjectId, @Validated(OnCreate.class) @RequestBody ChangeManagerRequestDTO dto) {
        subjectService.changeSubjectManager(subjectId, dto.newManagerLogin());
    }
}