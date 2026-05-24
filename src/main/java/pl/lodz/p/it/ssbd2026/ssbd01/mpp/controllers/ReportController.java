package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.ReportService;

import java.util.UUID;

@RequestMapping("/reports")
@RequiredArgsConstructor
@MethodCallLogged
@RestController
public class ReportController {

    private final ReportService reportService;
    @Operation(summary = "Wykonaj skanowanie", description = "Uruchom skanowanie repozytoriów za etykietą w przedmiocie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skanowanie ukończone pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono przedmiotu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono przedmiotu."))),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono tag", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Podany tag nie znaleziono w żadnym z repozytoriów."))),
            @ApiResponse(responseCode = "500", description = "Nieoczekiwany błąd", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Wystąpił błąd podczas klonowania repozytoriów.")))
    })
    @PostMapping("/demand/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void generateReportOnDemand(@PathVariable UUID subjectId, @RequestParam String tag) {
        reportService.generateReportOnDemand(subjectId, tag);
    }

    @GetMapping("/subject/{subjectId}")
    public void getAllReports(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
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