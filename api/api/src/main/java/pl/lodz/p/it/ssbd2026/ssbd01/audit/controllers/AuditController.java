package pl.lodz.p.it.ssbd2026.ssbd01.audit.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.audit.services.AuditService;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiSecuredResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.audit.AuditRecordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;

@RestController
@RequestMapping(value = "/admin/audit", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
@ApiSecuredResponses
@Tag(name = "Audyt", description = "Historia zmian w systemie dla Administratora")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Pobierz historię zmian (Javers)", description = "Pobiera stronicowaną i posortowaną listę logów audytowych. Umożliwia filtrowanie po typie encji.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historia zmian pobrana pomyślnie"),
    })
    @GetMapping
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public Page<AuditRecordDTO> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "commitDate") String sortBy,
            @RequestParam(defaultValue = "true") boolean sortDesc,
            @RequestParam(required = false) String entityFilter) {

        return auditService.getAuditHistory(page, size, sortBy, sortDesc, entityFilter);
    }
}