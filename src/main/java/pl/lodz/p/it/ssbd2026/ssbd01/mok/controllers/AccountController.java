package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.ChangeOwnPasswordDto;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AccountService;

import java.security.Principal;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    @PatchMapping("security/password")
    public ResponseEntity<Void> changeOwnPassword(@Valid @RequestBody ChangeOwnPasswordDto dto, @RequestHeader String ifMatch, Principal principal){

        String login = principal.getName();

        accountService.changeOwnPassword(
                login,
                dto.getOldPassword(),
                dto.getNewPassword(),
                ifMatch
        );


        return ResponseEntity.noContent().build();
    }


}
