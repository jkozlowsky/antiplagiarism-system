package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeEmailDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;
import org.springframework.http.ResponseEntity;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;

import java.security.Principal;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final VersionHashService versionHashService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO getAccount(@PathVariable UUID id) {

        Account account = accountService.getAccountWithAccessLevels(id);
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @GetMapping("/login/{login}")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO getAccountByLogin(@PathVariable String login){
        Account account = accountService.getAccountByLogin(login);
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountDTO> getAllAccounts(@RequestParam(required = false) String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return accountService.getAllAccounts().stream().map((account) -> accountMapper.toAccountDTO(
                    account,
                    versionHashService.hashObjectVersion(account.getId(), account.getVersion()))
            ).toList();
        } else {
            return accountService.searchAccounts(phrase).stream().map((account) -> accountMapper.toAccountDTO(
                    account,
                    versionHashService.hashObjectVersion(account.getId(), account.getVersion()))
            ).toList();
        }
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/grant")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO grantAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestBody AccountDTO accountDTO) {

        accountService.grantAccessLevel(id, accessLevelName, accountDTO.version());
        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                updatedAccount,
                versionHashService.hashObjectVersion(updatedAccount.getId(), updatedAccount.getVersion()));
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/revoke")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO revokeAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestBody AccountDTO accountDTO) {

        accountService.revokeAccessLevel(id, accessLevelName, accountDTO.version());
        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                updatedAccount,
                versionHashService.hashObjectVersion(updatedAccount.getId(), updatedAccount.getVersion()));
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


    @PostMapping("/security/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeOwnPassword(
            @Valid @RequestBody ChangeOwnPasswordDto dto,
            @RequestHeader("If-Match") String ifMatch,
            Principal principal) {

        String login = principal.getName();

        accountService.changeOwnPassword(
                login,
                dto.getOldPassword(),
                dto.getNewPassword(),
                ifMatch
        );
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @PostMapping("/block/{id}")
    public ResponseEntity<Void> blockUserAccount(@PathVariable UUID id) {
        accountService.blockUserAccount(id);
        return ResponseEntity.noContent().build();
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockUserAccount(@PathVariable UUID id) {
        accountService.unblockUserAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    public AccountWithAccessLevelsDTO updateAccount(
            @PathVariable UUID id,
            @RequestBody @Validated(OnUpdate.class) AccountDTO accountDTO
    ) {
        return accountService.updateAccount(
                id,
                accountDTO.versionHash(),
                accountMapper.toUpdateAccount(accountDTO)
        );
    }
}