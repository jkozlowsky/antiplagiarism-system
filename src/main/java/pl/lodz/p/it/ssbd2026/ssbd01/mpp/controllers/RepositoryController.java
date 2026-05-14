package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RepositoryService;

import java.util.UUID;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
@MethodCallLogged
public class RepositoryController  {
    private final RepositoryService repositoryService;
    
    @GetMapping("/subject/{subjectId}")
    public void getRepositories(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @GetMapping("/subject/{subjectId}/users")
    public void getUserAccounts(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @PostMapping("/{repositoryId}/check")
    public void checkOwnCode(@PathVariable UUID repositoryId, @RequestParam String tag) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}