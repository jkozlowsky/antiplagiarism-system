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
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.ReportMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.ReportDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentReportDetailsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

import java.util.List;
import java.util.UUID;

@RequestMapping("/reports")
@RequiredArgsConstructor
@MethodCallLogged
@RestController
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
    public ReportDTO generateReportOnDemand(@PathVariable UUID subjectId, @RequestParam String tag) {
        return reportMapper.toDto(reportService.generateReportOnDemand(subjectId, tag));
    }

    @Operation(summary = "Pobierz raporty", description = "Pobierz listę raportów dla podanego przedmiotu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zwrócono listę raportów"),
    })

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public Page<ReportDTO> getAllReports(@PathVariable UUID subjectId, @ParameterObject Pageable pageable) {
        return reportService.getAllReports(subjectId, pageable)
                .map(reportMapper::toDto);
    }
    @GetMapping("/my-reports")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public List<ReportDTO> getMyReports(java.security.Principal principal) {
        return reportService.getStudentOwnReports(principal.getName()).stream()
                .map(reportMapper::toDto)
                .toList();
    }

    @GetMapping("/my-reports/{reportId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    public StudentReportDetailsDTO getMyReportDetails(
            @PathVariable UUID reportId, java.security.Principal principal) {
        return reportService.getStudentReportDetails(reportId, principal.getName());
    }

    @GetMapping("/{reportId}")
    public void getReportDetails(@PathVariable UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @DeleteMapping("/{reportId}")
    public void deleteReport(@PathVariable UUID reportId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/student/{studentId}")
    public void getStudentOwnReports(@PathVariable UUID studentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/schedule")
    public void generateReportBySchedule() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}