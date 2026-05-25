package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RuleService;

import java.util.UUID;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
@MethodCallLogged
public class RuleController  {
    private final RuleService ruleService;
    
    @GetMapping("/subject/{subjectId}")
    public void getRules(@PathVariable UUID subjectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/subject/{subjectId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void createRulePreset(@PathVariable UUID subjectId,
                                 @Valid @RequestBody CreateRulePresetDTO ruleDto) {
        ruleService.createRulePreset(subjectId, ruleDto);
    }
    
    @PutMapping("/{ruleId}")
    public void updateRule(@PathVariable UUID ruleId, @RequestBody Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @DeleteMapping("/{ruleId}")
    public void deleteRule(@PathVariable UUID ruleId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}