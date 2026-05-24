package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RepositoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
@MethodCallLogged
public class RepositoryController {
    private final RepositoryService repositoryService;

    @GetMapping("/subject/{subjectId}")
    public void getRepositories(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/subject/{subjectId}/users")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Operation(summary = "Przeglądanie kont użytkowników", description = "Pobiera listę kont studentów w danym przedmiocie")
    @ApiResponse(responseCode = "200", description = "Poprawnie pobrano listę kont studentów w danym przedmiocie")
    public List<SubjectStudentStatsDTO> getUserAccounts(@PathVariable UUID subjectId) {
        return repositoryService.getUserAccounts(subjectId);
    }

    @PostMapping("/{repositoryId}/check")
    public void checkOwnCode(@PathVariable UUID repositoryId, @RequestParam String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}