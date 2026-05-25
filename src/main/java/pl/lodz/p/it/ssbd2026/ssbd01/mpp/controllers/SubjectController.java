package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

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