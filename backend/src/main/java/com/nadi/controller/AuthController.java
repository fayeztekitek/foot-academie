package com.nadi.controller;

import com.nadi.dto.AuthResponse;
import com.nadi.dto.LoginRequest;
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
        if (ancienMotDePasse == null || nouveauMotDePasse == null
                || ancienMotDePasse.isBlank() || nouveauMotDePasse.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (nouveauMotDePasse.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit contenir au moins 8 caractères"));
        }
        authService.changePassword(securityUtils.getCurrentUserOrThrow(), ancienMotDePasse, nouveauMotDePasse);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }
}
