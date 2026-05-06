package pl.lodz.p.it.ssbd2026.ssbd01.mok.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.AuthResponseDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.LoginRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.RefreshTokenRequestDTO;
import pl.lodz.p.it.ssbd2026.ssbd01.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.services.AuthService;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@MethodCallLogged
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();

        AuthResponseDTO response = authService.authenticate(loginRequest.login(), loginRequest.password(), ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        AuthResponseDTO response = authService.refreshToken(requestDTO.refreshToken(), ipAddress);
        return ResponseEntity.ok(response);
    }
}