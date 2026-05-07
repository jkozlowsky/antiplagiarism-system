package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.*;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AuthService;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                    HttpServletRequest request)

    {
        String ipAddress = request.getRemoteAddr();
        authService.authenticate(loginRequest.login(), loginRequest.password(), ipAddress);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponseDTO("mfa.required"));
    }

    @PostMapping("/resend2FA")
    public ResponseEntity<MessageResponseDTO> resend2FACode(@Valid @RequestBody Resend2FARequestDTO requestDTO) {
        authService.resend2FactorCode(requestDTO.login());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponseDTO("mfa.resend"));
    }

    @PostMapping("/check2FA")
    public ResponseEntity<AuthResponseDTO> verify2FACode(@Valid @RequestBody Verify2FARequestDTO requestDTO,
                                                         HttpServletRequest request,
                                                         @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String langHeader) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.verify2FactorCode(requestDTO.auth2F(), requestDTO.login(), ipAddress, langHeader);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.refreshToken(requestDTO.refreshToken(), ipAddress);
        return ResponseEntity.ok(response);
    }
}