package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.ssbd2026.ssbd01.config.i18n.Translator;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiSecuredResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ConfirmResetDto;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.reset_password.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.PasswordResetService;

@RestController
@RequestMapping("/auth/reset")
@RequiredArgsConstructor
@ApiSecuredResponses
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final Translator translator;

    @Operation(summary = "Zresetuj hasło za podanym e-mailem (etap 1)", description = "Wyślij kod na podany email o resetowaniu hasła")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie wysłano email"),
            @ApiResponse(responseCode = "200", description = "Nie znaleziono adresu email")
    })
    @PostMapping("/request")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> requestReset(@RequestBody @Validated(OnCreate.class) ResetPasswordDTO request) {
        passwordResetService.initiatePasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Zresetuj hasło za podanym e-mailem (etap 2)", description = "Weryfikuje kod z e-maila w procesie resetu hasła.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie zweryfikowano, zwraca tokeny JWT"),
            @ApiResponse(responseCode = "404", description = "Konto z takim e-mailem nie istnieje", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta powiązanego z tym adresem e-mail."))),
            @ApiResponse(responseCode = "409", description = "Błędny lub wygasły kod", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Kod logowania jest nieprawidłowy."))),
            @ApiResponse(responseCode = "409", description = "Niepoprawny token weryfikacyjny", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Czas na reset hasła minął lub token został już użyty.")))
    })
    @PostMapping("/confirm")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> confirmReset(@RequestBody @Validated(OnCreate.class) ConfirmResetDto request) {
        passwordResetService.resolvePasswordReset(request.token(), request.newPassword());
        String message = translator.translate("success.password.changed");
        return ResponseEntity.ok(message);
    }
}
