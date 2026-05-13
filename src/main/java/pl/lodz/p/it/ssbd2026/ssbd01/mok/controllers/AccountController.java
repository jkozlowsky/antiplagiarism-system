package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.VersionHashService;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final VersionHashService versionHashService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO getAccount(@PathVariable UUID id) {

        Account account = accountService.getAccountWithAccessLevels(id);
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @GetMapping("/login/{login}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO getAccountByLogin(@PathVariable String login) {
        Account account = accountService.getAccountByLogin(login);
        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @GetMapping("/login/no-access-level/{login}")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO getAccountByLoginNoAccessLevel(@PathVariable String login) {
        Account account = accountService.getAccountByLogin(login);
        return accountMapper.toAccountDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public Page<AccountWithAccessLevelsDTO> getAllAccounts(
            @RequestParam(required = false) String phrase,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Account> accountPage;

        if (phrase == null || phrase.trim().isEmpty()) {
            accountPage = accountService.getAllAccounts(page, size);
        } else {
            accountPage = accountService.searchAccounts(phrase, page, size);
        }

        return accountPage.map(account -> accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated(OnCreate.class) @RequestBody RegisterAccountDTO dto) {
        Account account = accountMapper.toEntity(dto);
        accountService.registerAccount(account);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {
        accountService.confirmActivation(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/grant")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO grantAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestHeader("If-Match") String ifMatch) {
        return accountService.grantAccessLevel(id, accessLevelName, ifMatch);
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}/revoke")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO revokeAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestHeader("If-Match") String ifMatch) {

        accountService.revokeAccessLevel(id, accessLevelName, ifMatch);

        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                updatedAccount,
                versionHashService.hashObjectVersion(updatedAccount.getId(), updatedAccount.getVersion()));
    }

    @PatchMapping("/{login}/email")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) and #login != authentication.name")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO adminChangeAccountEmail(
            @PathVariable String login,
            @Validated(OnCreate.class) @RequestBody ChangeEmailDTO changeEmailDTO,
            @RequestHeader("If-Match") String ifMatch) {

        Account account = accountService.changeEmailByAdmin(login, changeEmailDTO.email(), ifMatch);
        return accountMapper.toAccountDTONoHash(account);
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
    public ResponseEntity<Void> blockUserAccount(
            @PathVariable UUID id,
            @RequestHeader("If-Match") String ifMatch) {
        accountService.blockUserAccount(id, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockUserAccount(
            @PathVariable UUID id,
            @RequestHeader("If-Match") String ifMatch) {
        accountService.unblockUserAccount(id, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email/change")
    public ResponseEntity<Void> requestEmailChange(
            Authentication authentication) {
        String login = authentication.getName();
        accountService.createUrlOwnEmailChange(login);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<String> confirmEmailChange(@Valid @RequestBody OwnEmailChangeDTO request) {
        accountService.confirmOwnEmailChange(request.token(), request.password(), request.newEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/change/resend")
    public ResponseEntity<Void> resendEmailChangeRequest(Authentication authentication) {
        String login = authentication.getName();
        accountService.resendUrlOwnEmailChange(login);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/revert")
    public ResponseEntity<Void> revertEmailChange(@RequestParam String token) {
        accountService.revertEmailChange(token);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/{id}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO updateOtherAccount(
            @PathVariable UUID id,
            @RequestBody @Validated(OnUpdate.class) AccountDTO accountDTO,
            @RequestHeader("If-Match") String ifMatch
    ) {
        return accountService.updateAccount(
                id,
                ifMatch,
                accountMapper.toUpdateAccount(accountDTO)
        );
    }

    @PatchMapping("/update-myself")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO updateMyAccount(
            @Validated(OnUpdate.class) @RequestBody AccountDTO accountDTO,
            @RequestHeader("If-Match") String ifMatch,
            Principal principal) {

        String login = principal.getName();

        return accountService.updateAccountByLogin(
                login,
                ifMatch,
                accountMapper.toUpdateAccount(accountDTO)
        );
    }


    @PostMapping("/{login}/password")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) and #login != authentication.name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeOtherPassword(
            @PathVariable String login,
            @Valid @RequestBody ChangeOtherPasswordDto dto,
            @RequestHeader("If-Match") String ifMatch) {

        accountService.changeOtherPassword(
                login,
                dto.getNewPassword(),
                ifMatch
        );
    }

    @PostMapping("/role/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRoleLog(@RequestBody String role, Principal principal){
        log.info("{} changed his active role to: {}", principal.getName(), role);
    }

}