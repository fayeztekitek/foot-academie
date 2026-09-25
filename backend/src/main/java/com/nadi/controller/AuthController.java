package com.nadi.controller;

import com.nadi.dto.AuthResponse;
import com.nadi.dto.LoginRequest;
import com.nadi.security.PasswordPolicy;
import com.nadi.security.SecurityUtils;
import com.nadi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body) {
        String ancienMotDePasse = body.get("ancienMotDePasse");
        String nouveauMotDePasse = body.get("nouveauMotDePasse");
        if (ancienMotDePasse == null || ancienMotDePasse.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            PasswordPolicy.validateOrThrow(nouveauMotDePasse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        authService.changePassword(securityUtils.getCurrentUserOrThrow(), ancienMotDePasse, nouveauMotDePasse);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }
}
