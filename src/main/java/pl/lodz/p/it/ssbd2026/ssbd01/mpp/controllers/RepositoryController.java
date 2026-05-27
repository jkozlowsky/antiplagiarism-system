package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.StudentRepositoryDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.subject.SubjectStudentStatsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RepositoryService;

import java.security.Principal;
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

    @GetMapping("/subject/{subjectName}/users")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @Operation(summary = "Przeglądanie kont użytkowników", description = "Pobiera listę kont studentów w danym przedmiocie")
    @ApiResponse(responseCode = "200", description = "Poprawnie pobrano listę kont studentów w danym przedmiocie")
    public List<SubjectStudentStatsDTO> getUserAccounts(@PathVariable String subjectName) {
        return repositoryService.getUserAccounts(subjectName);
    }

    @PostMapping("/{repositoryId}/check")
    public void checkOwnCode(@PathVariable UUID repositoryId, @RequestParam String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/my-repositories")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_STUDENT)")
    @Operation(summary = "Pobierz repozytoria studenta - funkcja pomocnicza do MPP.20 na frontend")
    // do sprawdzenia, bo możliwe że dubluje kogoś przypadek użycia
    public List<StudentRepositoryDTO> getMyRepositories(Principal principal) {
        return repositoryService.getStudentRepositories(principal.getName());
    }
}