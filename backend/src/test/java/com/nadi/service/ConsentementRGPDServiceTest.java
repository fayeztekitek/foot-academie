package com.nadi.service;

import com.nadi.model.ConsentementRGPD;
import com.nadi.model.Parent;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.ConsentementRGPDRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.FamilyAccessGuard;
import com.nadi.security.SecurityUtils;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentementRGPDServiceTest {

    @Mock
    private ConsentementRGPDRepository consentementRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private ConsentementRGPDService consentementService;

    @BeforeEach
    void setUp() {
        FamilyAccessGuard guard = new FamilyAccessGuard(
                parentRepository, new SecurityUtils(utilisateurRepository));
        consentementService = new ConsentementRGPDService(consentementRepository, parentRepository, guard);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateParent(Long userId) {
        Utilisateur user = Utilisateur.builder()
                .id(userId).email("p@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_PARENT"))));
    }

    private Parent parent(Long id) {
        return Parent.builder().id(id).prenom("P").nom("N").email("p@nadi.tn").build();
    }

    @Test
    void parentCanListOwnConsents() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));
        when(consentementRepository.findByParentId(10L)).thenReturn(List.of());

        assertTrue(consentementService.getByParent(10L).isEmpty());
    }

    @Test
    void parentCannotListOtherConsents() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));

        assertThrows(AccessDeniedException.class, () -> consentementService.getByParent(11L));
    }

    @Test
    void parentCanRecordOwnConsent() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));
        when(parentRepository.findById(10L)).thenReturn(Optional.of(parent(10L)));
        when(consentementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ConsentementRGPD saved = consentementService.record(10L, "IMAGE", true, "127.0.0.1", null);

        assertTrue(saved.isAccord());
        assertEquals("127.0.0.1", saved.getAdresseIP());
    }

    @Test
    void parentCannotForgeOtherFamilyConsent() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L)).thenReturn(Optional.of(parent(10L)));

        assertThrows(AccessDeniedException.class,
                () -> consentementService.record(11L, "IMAGE", true, "1.2.3.4", null));
        verify(consentementRepository, never()).save(any());
    }

    @Test
    void exportContainsRegisterHeader() {
        when(consentementRepository.findAllByOrderByDateConsentementDesc()).thenReturn(List.of());

        String csv = consentementService.exportCsv();

        assertTrue(csv.startsWith("ID,Parent,Email"));
    }
}
