package com.nadi.security;

import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UtilisateurRepository utilisateurRepository;

    public Optional<Utilisateur> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        if (auth.getPrincipal() instanceof Utilisateur) {
            Utilisateur user = (Utilisateur) auth.getPrincipal();
            if (TenantContext.getTenantId() == null && user.getTenantId() != null) {
                TenantContext.setTenantId(user.getTenantId());
            }
            return Optional.of(user);
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

    public Utilisateur getCurrentUserOrThrow() {
        return getCurrentUser().orElseThrow(() -> new RuntimeException("Utilisateur non authentifié"));
    }

    public boolean isParent() {
        return getCurrentUser()
                .map(u -> u.getRole() == Role.PARENT)
                .orElse(false);
    }

    public boolean isAdmin() {
        return getCurrentUser()
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public boolean isSuperAdmin() {
        return getCurrentUser()
                .map(u -> u.getRole() == Role.SUPER_ADMIN)
                .orElse(false);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().map(Utilisateur::getId).orElse(null);
    }

    public Long getCurrentTenantId() {
        return TenantContext.getTenantId();
    }
}
