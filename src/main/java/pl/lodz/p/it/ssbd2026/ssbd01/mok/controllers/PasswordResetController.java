package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ConfirmResetDto;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.PasswordResetService;

@RestController
@RequestMapping("/auth/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestReset(@RequestBody @Validated(OnCreate.class) ResetPasswordDTO request) {
        passwordResetService.initiatePasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmReset(@RequestBody @Validated(OnCreate.class) ConfirmResetDto request) {
        passwordResetService.resolvePasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok("Password successfully changed.");
    }
}
