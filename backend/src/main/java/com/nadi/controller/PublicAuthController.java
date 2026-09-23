package com.nadi.controller;

import com.nadi.dto.TenantPublicResponse;
import com.nadi.model.Academie;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> adminResetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String newPassword = body.get("newPassword");
        if (email == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email et mot de passe (min 6 car.) requis"));
        }
        Long previousTenantId = TenantContext.getTenantId();
        try {
            TenantContext.clear();
            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Utilisateur non trouvé avec cet email: " + email));
            }
            user.setMotDePasseHash(passwordEncoder.encode(newPassword));
            user.setMustChangePassword(false);
            utilisateurRepository.save(user);
        } finally {
            if (previousTenantId != null) {
                TenantContext.setTenantId(previousTenantId);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé pour " + email));
    }
}
