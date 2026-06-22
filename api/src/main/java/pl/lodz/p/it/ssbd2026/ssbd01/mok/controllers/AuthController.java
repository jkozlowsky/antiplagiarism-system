package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.config.openApi.ApiPublicResponses;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AuthService;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
@Tag(name = "Autoryzacja", description = "Punkty końcowe służące do logowania i odświeżania sesji. Nie wymagają tokenu JWT.")
@ApiPublicResponses
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Zaloguj użytkownika (etap 1)", description = "Weryfikuje login i hasło, a następnie wysyła kod 2FA na adres e-mail użytkownika.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Poświadczenia poprawne, oczekiwanie na kod 2FA"),
            @ApiResponse(responseCode = "409", description = "Konto zablokowane lub nie zweryfikowane", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zablokowane lub nie zweryfikowane"))),
    })
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<MessageResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                    HttpServletRequest request)

    {
        String ipAddress = request.getRemoteAddr();
        authService.authenticate(loginRequest.login(), loginRequest.password(), ipAddress);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponseDTO("mfa.required"));
    }

    @Operation(summary = "Wyślij ponownie kod 2FA", description = "Generuje i wysyła nowy kod uwierzytelniania dwuetapowego na e-mail.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Kod wysłany ponownie"),
            @ApiResponse(responseCode = "202", description = "Nie znaleziono konta", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono użytkownika."))),
            @ApiResponse(responseCode = "409", description = "Konto zablokowane lub nie zweryfikowane", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zablokowane lub nie zweryfikowane")))
    })
    @PostMapping("/resend2FA")
    @PreAuthorize("permitAll()")
    public ResponseEntity<MessageResponseDTO> resend2FACode(@Valid @RequestBody Resend2FARequestDTO requestDTO) {
        authService.resend2FactorCode(requestDTO.login());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponseDTO("mfa.resend"));
    }

    @Operation(summary = "Weryfikuj kod 2FA (etap 2)", description = "Weryfikuje przesłany kod z e-maila. Po sukcesie zwraca tokeny dostępu JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie zweryfikowano kod, autoryzacja zakończona sukcesem"),
            @ApiResponse(responseCode = "409", description = "Błędny lub wygasły kod 2FA", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nieprawidłowy kod 2FA.")))
    })
    @PostMapping("/check2FA")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponseDTO> verify2FACode(@Valid @RequestBody Verify2FARequestDTO requestDTO,
                                                         HttpServletRequest request,
                                                         @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String langHeader) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.verify2FactorCode(requestDTO.auth2F(), requestDTO.login(), ipAddress, langHeader);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Odśwież tokeny JWT", description = "Wydaje nową parę tokenów (Access Token i Refresh Token) na podstawie ważnego Refresh Tokenu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zwraca nową parę tokenów"),
            @ApiResponse(responseCode = "401", description = "Refresh token wygasł, został unieważniony lub jest nieprawidłowy", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Token odświeżania jest nieważny."))),
            @ApiResponse(responseCode = "409", description = "Konto zablokowane", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zablokowane")))
    })
    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.refreshToken(requestDTO.refreshToken(), ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logowanie przez Google", description = "Weryfikuje Google ID Token i loguje użytkownika. Jeśli to pierwsze logowanie, zakłada konto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie zalogowano, zwraca tokeny JWT"),
            @ApiResponse(responseCode = "401", description = "Nieprawidłowy token Google", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Błąd weryfikacji tokenu Google."))),
            @ApiResponse(responseCode = "409", description = "Konto zablokowane lub nie zweryfikowane", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zablokowane lub nie zweryfikowane")))
    })
    @PostMapping("/google")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponseDTO> loginWithGoogle(@Valid @RequestBody GoogleLoginRequestDTO requestDTO, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.authenticateWithGoogle(requestDTO.idToken(), ipAddress);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logowanie samym e-mailem (etap 1)", description = "Wysyła jednorazowy kod autoryzacyjny na podany adres e-mail (tzw. Magic Link/Code).")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Kod został wysłany na e-mail"),
            @ApiResponse(responseCode = "404", description = "Konto z takim e-mailem nie istnieje", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta powiązanego z tym adresem e-mail."))),
            @ApiResponse(responseCode = "409", description = "Konto zablokowane lub nie zweryfikowane", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Konto zablokowane lub nie zweryfikowane")))
    })
    @PostMapping("/loginEmail")
    @PreAuthorize("permitAll()")
    public ResponseEntity<MessageResponseDTO> loginWithEmailOnly(@Validated(OnCreate.class) @RequestBody LoginWithEmailRequestDTO dto,
                                                                 HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        authService.authenticateOnlyWithEmail(dto.email(), ipAddress);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponseDTO("email.only.auth.required"));
    }

    @Operation(summary = "Weryfikuj logowanie e-mailem (etap 2)", description = "Weryfikuje kod z e-maila w procesie logowania bez hasła.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pomyślnie zweryfikowano, zwraca tokeny JWT"),
            @ApiResponse(responseCode = "404", description = "Konto z takim e-mailem nie istnieje", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Nie znaleziono konta powiązanego z tym adresem e-mail."))),
            @ApiResponse(responseCode = "409", description = "Błędny lub wygasły kod", content = @Content(mediaType = "text/plain", examples = @ExampleObject("Kod logowania jest nieprawidłowy.")))
    })
    @PostMapping("/checkEmail")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthResponseDTO>  verifySignInWithEmailOnlyCode(
            @Validated(OnCreate.class) @RequestBody VerifyEmailSignInRequestDTO dto,
            HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String langHeader) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.verifyEmailOnlyAuthCode(dto.token(), dto.email(), ipAddress, langHeader);
        return ResponseEntity.ok(response);
    }
}