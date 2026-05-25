package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RuleService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
@MethodCallLogged
public class RuleController  {
    private final RuleService ruleService;

    @Operation(summary = "Pobierz listę szablonów reguł", description = "Pobiera listę wszystkich dostępnych szablonów reguł.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista szablonów pobrana pomyślnie",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RulePresetDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Brak uwierzytelnienia", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<RulePresetDTO> getRulePresetsTemplates() {
        return ruleService.getRulePresetsTemplates();
    }

    
    @PostMapping("/subject/{subjectId}")
    public void createRulePreset(@PathVariable UUID subjectId, @RequestBody Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    
    @PutMapping("/{ruleId}")
    public void updateRulePreset(@PathVariable UUID ruleId, @RequestBody Object ruleDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRulePreset(
            @PathVariable UUID ruleId,
            @RequestHeader("If-Match") String ifMatch,
            Principal principal) {

        ruleService.deleteRulePreset(ruleId, ifMatch, principal.getName());
        return ResponseEntity.noContent().build();
    }
}