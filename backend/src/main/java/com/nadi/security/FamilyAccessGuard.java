package com.nadi.security;

import com.nadi.model.Joueur;
import com.nadi.model.Parent;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Enforces family-level isolation within a tenant.
 * The Hibernate tenant filter isolates tenants, not families: without these
 * checks any PARENT could read or modify another family's children data
 * by passing an arbitrary parentId/joueurId.
 * Staff roles (SUPER_ADMIN, ADMIN, COACH) are not family-restricted.
 */
@Component
@RequiredArgsConstructor
public class FamilyAccessGuard {

    private final ParentRepository parentRepository;
    private final SecurityUtils securityUtils;

    /** Returns the caller's own parentId, enforcing it equals the requested one. */
    public Long requireOwnParentId(Long requestedParentId) {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        if (user.getRole() != Role.PARENT) {
            return requestedParentId;
        }
        Long ownParentId = parentRepository.findByUtilisateurId(user.getId())
                .map(Parent::getId)
                .orElseThrow(() -> new AccessDeniedException("Profil parent introuvable"));
        if (requestedParentId == null || !ownParentId.equals(requestedParentId)) {
            throw new AccessDeniedException("Accès interdit à ces données familiales");
        }
        return ownParentId;
    }

    /** Ensures a PARENT caller may only access the given player's data. */
    public void requireAccessToJoueur(Joueur joueur) {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        if (user.getRole() != Role.PARENT) {
            return;
        }
        Long ownParentId = parentRepository.findByUtilisateurId(user.getId())
                .map(Parent::getId)
                .orElseThrow(() -> new AccessDeniedException("Profil parent introuvable"));
        if (joueur == null || joueur.getParent() == null
                || !ownParentId.equals(joueur.getParent().getId())) {
            throw new AccessDeniedException("Accès interdit à ces données familiales");
        }
    }
}
