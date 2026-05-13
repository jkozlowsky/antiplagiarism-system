package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2026.ssbd01.config.i18n.Translator;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ConfirmResetDto;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.PasswordResetService;

@RestController
@RequestMapping("/auth/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final Translator translator;

    @PostMapping("/request")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> requestReset(@RequestBody @Validated(OnCreate.class) ResetPasswordDTO request) {
        passwordResetService.initiatePasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> confirmReset(@RequestBody @Validated(OnCreate.class) ConfirmResetDto request) {
        passwordResetService.resolvePasswordReset(request.token(), request.newPassword());
        String message = translator.translate("success.password.changed");
        return ResponseEntity.ok(message);
    }
}
