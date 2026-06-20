package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiSecuredResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ReportMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ReportDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.TeacherReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RequestMapping("/reports")
@RequiredArgsConstructor
@MethodCallLogged
@RestController
@ApiSecuredResponses
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @Operation(summary = "Wykonaj skanowanie", description = "Uruchom skanowanie repozytoriów za etykietą w przedmiocie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skanowanie ukończone pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono przedmiotu."))),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono tag", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Podany tag nie znaleziono w żadnym z repozytoriów."))),
            @ApiResponse(responseCode = "500", description = "Nieoczekiwany błąd", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Wystąpił błąd podczas klonowania repozytoriów.")))
    })
    @PostMapping("/demand/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public ReportDTO generateReportOnDemand(@PathVariable UUID subjectId, @RequestParam String tag, Principal principal) {
        return reportMapper.toDto(reportService.generateReportOnDemand(subjectId, tag, principal.getName()));
    }

    @Operation(summary = "Pobierz raporty", description = "Pobierz listę raportów dla podanego przedmiotu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zwrócono listę raportów"),
    })
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<ReportDTO> getAllReportsForSubject(@PathVariable UUID subjectId, @ParameterObject Pageable pageable) {
        return reportService.getAllReportsForSubject(subjectId, pageable)
                .map(reportMapper::toDto);
    }

    @Operation(summary = "Pobierz swoje raporty", description = "Pobierz listę raportów dla swojego konta użytkownika")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zwrócono listę raportów"),
    })
    @GetMapping("/teacher")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<ReportDTO> getAllReportsForTeacher(Principal principal, @ParameterObject Pageable pageable) {
        return reportService.getAllReportsForTeacher(principal.getName(), pageable)
                .map(reportMapper::projectionToDto);
    }

    @Operation(summary = "Pobierz moje raporty", description = "Zwraca listę raportów do których student ma wgląd w oparciu o politykę prywatności.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista raportów pobrana pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia (np. brak lub nieważny token)")
    })
    @GetMapping("/my-reports")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<ReportDTO> getMyReports(Principal principal) {
        return reportService.getStudentOwnReports(principal.getName());
    }

    @Operation(summary = "Szczegóły mojego raportu", description = "Pobiera wynik analizy antyplagiatowej dla wybranego raportu wyselekcjonowanej specjalnie dla studenta. Inne dane są zanonimizowane.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Raport pobrany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do raportu lub raport został ukryty"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono raportu o podanym identyfikatorze")
    })
    @GetMapping("/my-reports/{reportId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getMyReportDetails(@PathVariable UUID reportId, Principal principal) {
        return reportService.getStudentReportDetails(reportId, principal.getName());
    }

    @Operation(summary = "Szczegóły raportu dla Prowadzącego", description = "Pobiera szczegółowy wynik analizy antyplagiatowej, zawierający stopień podobieństwa oraz listę poszczególnych repozytoriów.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Raport pobrany pomyślnie"),
            @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia"),
            @ApiResponse(responseCode = "403", description = "Brak dostępu do raportu (nie przypisano prowadzącego do przedmiotu)"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono rekordu raportu o podanym identyfikatorze"),
            @ApiResponse(responseCode = "500", description = "Nie można załadować szczegółów raportu. Plik może być uszkodzony lub usunięty")
    })
    @GetMapping("/{reportId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public TeacherReportDetailsDTO getReportDetails(@PathVariable UUID reportId, Principal principal) {
        return reportService.getTeacherReportDetails(reportId, principal.getName());
    }

    @Operation(summary = "Usuń raport", description = "Usuwa raporty z bazy oraz pliki z maszyny")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usunięto raport"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono raportu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Podany raport nie istnieje."))),
    })
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void deleteReport(@PathVariable UUID reportId) {
        reportService.deleteReport(reportId);
    }

    @GetMapping("/student/{studentId}")
    public void getStudentOwnReports(@PathVariable UUID studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/schedule")
    public void generateReportBySchedule() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(summary = "Sprawdzenie swojego kodu", description = "Student może sprawdzić swój kod zgodnie z ustawioną obecnie regułą. Warunkiem skanowania jest posiadanie odpowiedniej liczby dostępnych skanowań.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skanowanie kodu ukończone pomyślnie."),
            @ApiResponse(responseCode = "403", description = "Brak dostępnych skanowań."),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono danego repozytorium."),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej.")
    })
    @PostMapping("/student-scan/{repositoryId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO generateStudentReport(
            @PathVariable UUID repositoryId,
            @RequestParam String studentTag,
            @RequestParam String taskTag,
            Principal principal) {
        return reportService.generateStudentReport(repositoryId, studentTag, taskTag, principal.getName());
    }
}