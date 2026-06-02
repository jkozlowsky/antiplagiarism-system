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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.*;
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

    @Operation(summary = "Synchronizuj z Gitea (MPP.1)", description = "Pobiera najnowszą listę repozytoriów z przypisanej organizacji na Gitea, pobiera ich współtwórców i aktualizuje stan w bazie danych.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pomyślnie zsynchronizowano przedmioty z platformą Gitea"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu"),
            @ApiResponse(responseCode = "500", description = "Błąd komunikacji z zewnętrznym API Gitea")
    })
    @PostMapping("/{subjectId}/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public void syncSubjectWithGitea(@PathVariable UUID subjectId) {
        subjectService.syncSubjectWithGitea(subjectId);
    }

    @Operation(summary = "Pobierz listę przedmiotów", description = "Zwraca listę wszystkich przedmiotów, do których zalogowany użytkownik ma dostęp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę przedmiotów")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @Operation(summary = "Pobierz przypisaną kadrę", description = "Zwraca listę wszystkich nauczycieli przypisanych do wskazanego przedmiotu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano listę nauczycieli"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono podanego przedmiotu")
    })
    @GetMapping("/{subjectId}/users")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    public ResponseEntity<List<TeacherAssignmentDTO>> getSubjectUsers(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(subjectService.getSubjectUsers(subjectId));
    }

    @Operation(summary = "Pobierz szczegóły przedmiotu", description = "Zwraca dokładne dane wybranego przedmiotu wraz z wliczonymi uprawnieniami zalogowanego użytkownika.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano szczegóły przedmiotu"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono podanego przedmiotu")
    })

    @GetMapping("/{subjectId}")
    @PreAuthorize("hasAnyAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN, T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public ResponseEntity<SubjectDTO> getSubjectDetails(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(subjectService.getSubjectDetails(subjectId));
    }

    @GetMapping("/{subjectId}/student")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public ResponseEntity<StudentSubjectDetailsDTO> getStudentSubjectDetails(@PathVariable UUID subjectId) {
        return ResponseEntity.ok(subjectService.getStudentSubjectDetails(subjectId));
    }

    @Operation(summary = "Utwórz nowy przedmiot (konfiguracja ręczna) (MPP.7)", description = "Tworzy nowy przedmiot z ręczną konfiguracją reguł i przypisuje do niego aktualnie zalogowanego prowadzącego.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Przedmiot został poprawnie utworzony"),
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
            @ApiResponse(responseCode = "409", description = "Użytkownik nie posiada aktywnego poziomu dostępu Nauczyciel", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/template")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubjectFromTemplate(@Validated(OnCreate.class) @RequestBody CreateSubjectTemplateDTO dto, Principal principal) {
        String userLogin = principal.getName();
        subjectService.createSubjectFromTemplate(dto, userLogin);
    }

    @Operation(summary = "Aktualizuj przedmiot (MPP.8)", description = "Aktualizuje podstawowe dane przedmiotu, zasady oraz przypisanych menedżerów.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Przedmiot został pomyślnie zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu"),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej - dane uległy zmianie w międzyczasie")
    })
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