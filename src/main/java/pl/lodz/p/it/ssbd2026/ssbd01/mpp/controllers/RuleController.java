package pl.lodz.p.it.ssbd2026.ssbd01.mpp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.RulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.CreateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.mpp.UpdateRulePresetDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mpp.services.RuleService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
@MethodCallLogged
public class RuleController {
    private final RuleService ruleService;

    @Operation(summary = "Pobierz listę szablonów reguł", description = "Pobiera listę wszystkich dostępnych szablonów reguł.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista szablonów pobrana pomyślnie",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RulePresetDTO.class))))
    })
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public List<RulePresetDTO> getRulePresetsTemplates() {
        return ruleService.getRulePresetsTemplates();
    }

    @Operation(
            summary = "Utworzenie predefiniowanego szablonu reguł",
            description = "Tworzy nowy, globalny szablon konfiguracji reguł, który może być później wykorzystany jako podstawa dla przedmiotów."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Szablon reguł został pomyślnie utworzony"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono słownika dla poziomu raportów")
    })
    @PostMapping("/templates")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void createRulePresetTemplate(
            @Valid @RequestBody CreateRulePresetDTO ruleDto) {
        ruleService.createRulePreset(ruleDto);
    }

    @Operation(summary = "Aktualizuj regułę", description = "Modyfikuje istniejącą regułę na podstawie przekazanego ID oraz nowych danych. Wymaga uprawnień nauczyciela.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reguła zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono reguły o podanym ID lub wskazany poziom raportu nie istnieje", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "Dane zostały zmodyfikowane przez innego użytkownika", content = @Content(mediaType = "text/plain"))
    })
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    public void updateRulePreset(
            @PathVariable UUID ruleId,
            @RequestHeader("If-Match") String ifMatch,
            @Valid @RequestBody UpdateRulePresetDTO ruleDto) {

        ruleService.updateRulePreset(ruleId, ruleDto, ifMatch);
    }

    @Operation(summary = "Usuwanie globalnego szablonu reguł (MPP.13)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Szablon reguł został pomyślnie usunięty"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono podanego szablonu reguł"),
            @ApiResponse(responseCode = "409", description = "Dane zostały zmodyfikowane przez innego użytkownika")
    })
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_TEACHER)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRulePreset(
            @PathVariable UUID ruleId,
            @RequestHeader("If-Match") String ifMatch) {

        ruleService.deleteRulePreset(ruleId, ifMatch);
        return ResponseEntity.noContent().build();
    }
}