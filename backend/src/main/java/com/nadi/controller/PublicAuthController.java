package com.nadi.controller;

import com.nadi.dto.TenantPublicResponse;
import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.UtilisateurRepository;
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

    private final AcademieRepository academieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/tenants")
    public List<TenantPublicResponse> getActiveTenants() {
        return academieRepository.findAll().stream()
                .filter(Academie::getActive)
                .map(a -> TenantPublicResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom())
                        .slug(a.getSlug())
                        .ville(a.getVille())
                        .logoUrl(a.getLogoUrl())
                        .build())
                .toList();
    }

    @PostMapping("/admin/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> adminResetPassword(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String newPassword = (String) body.get("newPassword");
        Object tenantIdObj = body.get("tenantId");
        if (email == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email et mot de passe (min 6 car.) requis"));
        }
        String hash = passwordEncoder.encode(newPassword);
        if (tenantIdObj != null) {
            Long tenantId = Long.parseLong(tenantIdObj.toString());
            log.info("Admin reset password: updating password for email='{}' in tenantId={}", email, tenantId);
            int updated = utilisateurRepository.updatePasswordByEmailAndTenantId(email, hash, tenantId);
            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("message", "Utilisateur non trouvé avec cet email dans ce tenant"));
            }
        } else {
            log.info("Admin reset password: updating password for email='{}' in ALL tenants", email);
            utilisateurRepository.updatePasswordByEmail(email, hash);
        }
        log.info("Admin reset password: done for email='{}'", email);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé pour " + email));
    }
}
