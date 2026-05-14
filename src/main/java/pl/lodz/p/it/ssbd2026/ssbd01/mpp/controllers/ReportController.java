package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import lombok.RequiredArgsConstructor;
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

    @PostMapping("/demand/{subjectId}")
    public void generateReportOnDemand(@PathVariable UUID subjectId, @RequestParam String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
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