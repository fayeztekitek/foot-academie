package com.nadi.security;

import com.nadi.model.Joueur;
import com.nadi.model.Parent;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyAccessGuardTest {

    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private FamilyAccessGuard guard;

    @BeforeEach
    void setUp() {
        guard = new FamilyAccessGuard(parentRepository, new SecurityUtils(utilisateurRepository));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Role role, Long userId) {
        Utilisateur user = Utilisateur.builder()
                .id(userId).email("u@nadi.tn").motDePasseHash("hash")
                .role(role).actif(true).tenantId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private Parent parent(Long id) {
        return Parent.builder().id(id).prenom("P").nom("N").email("p@nadi.tn").build();
    }

    private Joueur joueurOf(Long parentId) {
        Joueur joueur = Joueur.builder().prenom("J").nom("N")
                .dateNaissance(java.time.LocalDate.of(2015, 1, 1)).build();
        joueur.setParent(parent(parentId));
        return joueur;
    }

    @Test
    void parentCanAccessOwnFamily() {
        authenticateAs(Role.PARENT, 42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));

        assertEquals(10L, guard.requireOwnParentId(10L));
        assertDoesNotThrow(() -> guard.requireAccessToJoueur(joueurOf(10L)));
    }

    @Test
    void parentCannotAccessOtherFamily() {
        authenticateAs(Role.PARENT, 42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));

        assertThrows(AccessDeniedException.class, () -> guard.requireOwnParentId(11L));
        assertThrows(AccessDeniedException.class, () -> guard.requireAccessToJoueur(joueurOf(11L)));
    }

    @Test
    void adminBypassesFamilyCheckWithoutRepositoryCall() {
        authenticateAs(Role.ADMIN, 1L);

        assertEquals(99L, guard.requireOwnParentId(99L));
        assertDoesNotThrow(() -> guard.requireAccessToJoueur(joueurOf(99L)));
        verifyNoInteractions(parentRepository);
    }

    @Test
    void coachBypassesFamilyCheck() {
        authenticateAs(Role.COACH, 2L);

        assertDoesNotThrow(() -> guard.requireAccessToJoueur(joueurOf(77L)));
    }

    @Test
    void parentWithoutProfileIsDenied() {
        authenticateAs(Role.PARENT, 42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> guard.requireOwnParentId(10L));
    }
}
