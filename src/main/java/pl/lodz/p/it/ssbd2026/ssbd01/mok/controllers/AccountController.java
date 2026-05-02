package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeEmailDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO getAccount(@PathVariable UUID id) {

        Account account = accountService.getAccountWithAccessLevels(id);
        return accountMapper.toAccountWithAccessLevelsDTO(account);
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/grant")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO grantAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestBody AccountDTO accountDTO) {

        accountService.grantAccessLevel(id, accessLevelName, accountDTO.version());
        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsDTO(updatedAccount);
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/revoke")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO revokeAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestBody AccountDTO accountDTO) {

        accountService.revokeAccessLevel(id, accessLevelName, accountDTO.version());
        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsDTO(updatedAccount);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO adminChangeAccountEmail(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeEmailDTO changeEmailDTO) {

        Account account = accountService.changeEmailByAdmin(id, changeEmailDTO.email(), changeEmailDTO.version());
        return accountMapper.toAccountDTO(account);
    }
}