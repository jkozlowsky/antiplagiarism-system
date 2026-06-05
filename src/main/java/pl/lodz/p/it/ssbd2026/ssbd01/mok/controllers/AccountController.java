package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiPublicResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiSecuredResponses;
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
@Tag(name = "Konta", description = "Api służące do zarządzania kontami użytkowników")
@ApiSecuredResponses
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final VersionHashService versionHashService;

    @Operation(summary = "Pobierz konto po ID", description = "Pobiera szczegóły konta wraz z poziomami dostępu na podstawie UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poprawnie pobrano dane konta"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nie znaleziono zasobu",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Nie znaleziono konta o podanym ID.")
                    )
            )
    })
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

    @Operation(summary = "Pobierz konto po loginie", description = "Pobiera szczegóły konta wraz z poziomami dostępu na podstawie loginu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poprawnie pobrano dane konta"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nie znaleziono zasobu",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Nie znaleziono konta o podanym loginie.")
                    )
            )
    })
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

    @Operation(summary = "Pobierz podstawowe dane konta po loginie", description = "Pobiera podstawowe szczegóły konta (bez poziomów dostępu) na podstawie loginu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poprawnie pobrano dane konta"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nie znaleziono zasobu",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Nie znaleziono konta o podanym loginie.")
                    )
            )
    })
    @GetMapping("/login/no-access-level/{login}")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO getAccountByLoginNoAccessLevel(@PathVariable String login) {
        Account account = accountService.getAccountByLogin(login);
        return accountMapper.toAccountDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        );
    }

    @Operation(summary = "Pobierz wszystkie konta", description = "Pobiera stronicowaną listę wszystkich kont, opcjonalnie filtrowaną po frazie.")
    @ApiResponse(responseCode = "200", description = "Poprawnie pobrano listę kont")
    @GetMapping
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public Page<AccountWithAccessLevelsDTO> getAllAccounts(
            @RequestParam(required = false, name = "page") Integer page,
            @RequestParam(required = false, name = "size") Integer size,
            @RequestParam(required = false, name = "sortBy") String sortBy,
            @RequestParam(required = false, name = "sortDesc") Boolean sortDesc,
            @RequestParam(required = false, name = "phrase") String phrase,
            Principal principal) {

        String username = principal.getName();
        Page<Account> accountPage;

        if (phrase == null || phrase.trim().isEmpty()) {
            accountPage = accountService.getAllAccounts(page, size, sortBy, sortDesc, username);
        } else {
            accountPage = accountService.searchAccounts(phrase, page, size, sortBy, sortDesc, username);
        }

        return accountPage.map(account -> accountMapper.toAccountWithAccessLevelsAndVersionHashDTO(
                account,
                versionHashService.hashObjectVersion(account.getId(), account.getVersion())
        ));
    }

    @Operation(summary = "Zarejestruj konto", description = "Tworzy nowe konto w systemie na podstawie przesłanych danych.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Żądanie rejestracji przyjęte (konto oczekuje na aktywację)"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Konflikt - podany login lub email już istnieje",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Konflikt - podany login lub email już istnieje")
                    )
            )
    })
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> register(@Validated(OnCreate.class) @RequestBody RegisterAccountDTO dto) {
        Account account = accountMapper.toEntity(dto);
        accountService.registerAccount(account);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Aktywuj konto", description = "Aktywuje konto użytkownika na podstawie tokenu przesłanego na email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Konto zostało pomyślnie aktywowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono tokenu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Token aktywacyjny jest nieprawidłowy lub wygasł.")))
    })
    @PostMapping("/activate")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> activate(@RequestParam String token) {
        accountService.confirmActivation(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Przyznaj poziom dostępu", description = "Nadaje wybranemu kontu konkretny poziom dostępu. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Poziom dostępu został pomyślnie nadany"),
            @ApiResponse(responseCode = "404", description = "Brak zasobu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto lub poziom dostępu nie istnieje."))),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto posiada już ten poziom dostępu."))),
    })
    @PostMapping("/{id}/access-levels/{accessLevelName}/grant")
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO grantAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestHeader("If-Match") String ifMatch) {
        return accountService.grantAccessLevel(id, accessLevelName, ifMatch);
    }

    @Operation(summary = "Odbierz poziom dostępu", description = "Odbiera wybranemu kontu konkretny poziom dostępu (rolę). Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Poziom dostępu został pomyślnie odebrany"),
            @ApiResponse(responseCode = "404", description = "Brak zasobu", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto lub poziom dostępu nie istnieje."))),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto nie posiadało tego poziomu dostępu."))),
    })
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

    @Operation(summary = "Zmień adres e-mail (Administrator)", description = "Zmienia adres e-mail dowolnego konta przez administratora. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres e-mail został pomyślnie zmieniony"),
            @ApiResponse(responseCode = "404", description = "Konto nie istnieje", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta."))),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Podany adres e-mail jest już w użyciu."))),
    })
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

    @Operation(summary = "Zmień własne hasło", description = "Pozwala zalogowanemu użytkownikowi zmienić swoje hasło. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hasło zostało pomyślnie zmienione"),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Niezgodność wersji danych (If-Match).")))
    })
    @PostMapping("/security/password")
    @PreAuthorize("isAuthenticated()")
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

    @Operation(summary = "Zablokuj konto", description = "Pozwala administratorowi ręcznie zablokować konto użytkownika. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Konto zostało pomyślnie zablokowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta o podanym ID."))),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto jest już zablokowane."))),
    })
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @PostMapping("/block/{id}")
    public ResponseEntity<Void> blockUserAccount(
            @PathVariable UUID id,
            @RequestHeader("If-Match") String ifMatch) {
        accountService.blockUserAccount(id, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Odblokuj konto", description = "Pozwala administratorowi ręcznie odblokować zablokowane konto użytkownika. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Konto zostało pomyślnie odblokowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta o podanym ID."))),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto jest już odblokowane."))),
    })
    @MethodCallLogged
    @PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockUserAccount(
            @PathVariable UUID id,
            @RequestHeader("If-Match") String ifMatch) {
        accountService.unblockUserAccount(id, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Zarządaj zmiany e-maila", description = "Inicjuje proces zmiany własnego adresu e-mail przez wysłanie tokenu autoryzacyjnego.")
    @ApiResponse(responseCode = "200", description = "Proces zainicjowany, wiadomość e-mail została wysłana")
    @PostMapping("/email/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> requestEmailChange(
            Authentication authentication) {
        String login = authentication.getName();
        accountService.createUrlOwnEmailChange(login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Potwierdź zmianę e-maila", description = "Weryfikuje token i aktualizuje adres na nowy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres e-mail został zmieniony poprawnie"),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Podany nowy adres e-mail jest już zajęty.")))
    })
    @PostMapping("/email/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> confirmEmailChange(@Valid @RequestBody OwnEmailChangeDTO request) {
        accountService.confirmOwnEmailChange(request.token(), request.password(), request.newEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Wyślij ponownie link do zmiany e-maila", description = "Ponownie wysyła token autoryzacyjny.")
    @ApiResponse(responseCode = "200", description = "Wiadomość z linkiem wysłana ponownie")
    @PostMapping("/email/change/resend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resendEmailChangeRequest(Authentication authentication) {
        String login = authentication.getName();
        accountService.resendUrlOwnEmailChange(login);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cofnij zmianę e-maila", description = "Przywraca stary adres e-mail w przypadku omyłkowej zmiany.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adres e-mail został zmieniony poprawnie"),
            @ApiResponse(responseCode = "409", description = "Konflikt", content = @Content(mediaType = "text/plain", examples = @ExampleObject("czas życia tokenu odświeżającego wygasł")))
    })
    @PostMapping("/email/revert")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> revertEmailChange(@RequestParam String token) {
        accountService.revertEmailChange(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Aktualizuj konto użytkownika", description = "Modyfikuje podstawowe dane konta użytkownika przez administratora. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Konto zostało pomyślnie zaktualizowane"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta."))),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zostało zmodyfikowane, odśwież stronę.")))
    })
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

    @Operation(summary = "Aktualizuj konto użytkownika", description = "Modyfikuje podstawowe dane konta użytkownika przez administratora. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Konto zostało pomyślnie zaktualizowane"),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zostało zmodyfikowane, odśwież stronę.")))
    })
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

    @Operation(summary = "Zmień hasło innego użytkownika", description = "Administrator wymusza zmianę hasła na koncie innego użytkownika. Wymaga nagłówka If-Match.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hasło użytkownika zostało pomyślnie zmienione"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono konta", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono użytkownika."))),
            @ApiResponse(responseCode = "409", description = "Błąd blokady optymistycznej", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zostało zmodyfikowane, odśwież stronę.")))
    })
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

    @Operation(summary = "Logowanie zmiany roli", description = "Zapisuje w logach serwera zmianę poziomu dostępu/roli w panelu UI.")
    @ApiResponse(responseCode = "204", description = "Zdarzenie zmiany roli zalogowane pomyślnie", content = @Content)
    @PostMapping("/role/change")
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRoleLog(@RequestBody String role, Principal principal, jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        log.info("User access level changed | User: {} | Logical address (IP): {} | New Role: {}", principal.getName(), ipAddress, role);
    }

    @Operation(summary = "Odblokuj konto po dłuższej nieaktywności", description = "Umożliwia odblokowanie wygasłego konta dedykowanym tokenem.")
    @ApiResponse(responseCode = "204", description = "Konto zostało pomyślnie odblokowane", content = @Content)
    @PostMapping("/unblock-inactive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> unblockAccountViaToken(@RequestParam String token) {
        accountService.unblockAccountViaEmailToken(token);
        return ResponseEntity.noContent().build();
    }
}