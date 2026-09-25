package com.nadi.controller;

import com.nadi.dto.TenantPublicResponse;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.service.AcademieService;
import com.nadi.security.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PublicAuthController {

    private final AcademieService academieService;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/tenants")
    public List<TenantPublicResponse> getActiveTenants() {
        return academieService.getActiveTenants();
    }

    @PostMapping("/admin/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> adminResetPassword(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String newPassword = (String) body.get("newPassword");
        Object tenantIdObj = body.get("tenantId");
        // Tenant scoping is mandatory: an unscoped reset would overwrite the
        // password (and revoke sessions) of every tenant sharing this email.
        if (email == null || email.isBlank() || tenantIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email et tenantId requis"));
        }
        try {
            PasswordPolicy.validateOrThrow(newPassword);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        String hash = passwordEncoder.encode(newPassword);
        Long tenantId;
        try {
            tenantId = Long.parseLong(tenantIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "tenantId invalide"));
        }
        log.info("Admin reset password: updating password for email='{}' in tenantId={}", email, tenantId);
        int updated = utilisateurRepository.updatePasswordByEmailAndTenantId(email, hash, tenantId);
        if (updated == 0) {
            return ResponseEntity.status(404).body(Map.of("message", "Utilisateur non trouvé avec cet email dans ce tenant"));
        }
        log.info("Admin reset password: done for email='{}' in tenantId={}", email, tenantId);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé pour " + email));
    }
}
