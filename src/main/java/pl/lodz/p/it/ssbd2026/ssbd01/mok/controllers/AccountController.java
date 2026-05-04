package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AccountWithAccessLevelsDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RegisterAccountDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.converters.AccountMapper;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.entities.Account;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;

import java.util.UUID;

@RestController
@RequestMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class  AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated(OnCreate.class) @RequestBody RegisterAccountDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error.passwords.mismatch");
        }
        Account account = accountMapper.toEntity(dto);
        accountService.registerAccount(account);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {
        accountService.confirmActivation(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/access-levels/{accessLevelName}")
    @ResponseStatus(HttpStatus.OK)
    public AccountWithAccessLevelsDTO grantAccessLevel(
            @PathVariable UUID id,
            @PathVariable String accessLevelName,
            @RequestBody AccountDTO accountDTO) {

        accountService.grantAccessLevel(id, accessLevelName, accountDTO.version());
        Account updatedAccount = accountService.getAccountWithAccessLevels(id);

        return accountMapper.toAccountWithAccessLevelsDTO(updatedAccount);
    }
}