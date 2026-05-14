package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public void createRule(@PathVariable UUID subjectId, @RequestBody Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
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