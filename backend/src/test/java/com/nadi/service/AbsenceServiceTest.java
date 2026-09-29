package com.nadi.service;

import com.nadi.dto.AbsenceResponse;
import com.nadi.model.*;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.JoueurRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private CreneauRepository creneauRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private AbsenceService absenceService;

    @BeforeEach
    void setUp() {
        SecurityUtils securityUtils = new SecurityUtils(utilisateurRepository);
        FamilyAccessGuard guard = new FamilyAccessGuard(parentRepository, securityUtils);
        absenceService = new AbsenceService(absenceRepository, creneauRepository,
                joueurRepository, parentRepository, guard, securityUtils);
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

    private Joueur joueur(Long parentId) {
        Joueur joueur = Joueur.builder().prenom("J").nom("N")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(1L);
        joueur.setParent(Parent.builder().id(parentId).prenom("P").nom("N").email("p@nadi.tn").build());
        return joueur;
    }

    @Test
    void parentCanDeclareOwnChildAbsence() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(creneauRepository.findById(2L)).thenReturn(Optional.of(
                Creneau.builder().jourSemaine(JourSemaine.LUNDI)
                        .heureDebut(java.time.LocalTime.of(16, 0))
                        .heureFin(java.time.LocalTime.of(17, 0)).build()));
        when(absenceRepository.findByJoueurIdAndCreneauIdAndDateSeance(eq(1L), eq(2L), any()))
                .thenReturn(Optional.empty());
        when(absenceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AbsenceResponse response = absenceService.declareAbsence(
                1L, 2L, LocalDate.now().plusDays(2), "Rendez-vous médical");

        assertEquals("Rendez-vous médical", response.getMotif());
        assertFalse(response.getPresent());
    }

    @Test
    void parentCannotDeclareOtherFamilyAbsence() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(11L)));

        assertThrows(AccessDeniedException.class, () ->
                absenceService.declareAbsence(1L, 2L, LocalDate.now().plusDays(2), "x"));
        verify(absenceRepository, never()).save(any());
    }

    @Test
    void pastDateIsRejected() {
        authenticateParent(42L);

        assertThrows(RuntimeException.class, () ->
                absenceService.declareAbsence(1L, 2L, LocalDate.now().minusDays(1), "trop tard"));
        verify(absenceRepository, never()).save(any());
    }

    @Test
    void duplicateDeclarationIsRejected() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(creneauRepository.findById(2L)).thenReturn(Optional.of(
                Creneau.builder().jourSemaine(JourSemaine.LUNDI)
                        .heureDebut(java.time.LocalTime.of(16, 0))
                        .heureFin(java.time.LocalTime.of(17, 0)).build()));
        when(absenceRepository.findByJoueurIdAndCreneauIdAndDateSeance(eq(1L), eq(2L), any()))
                .thenReturn(Optional.of(Absence.builder().build()));

        assertThrows(RuntimeException.class, () ->
                absenceService.declareAbsence(1L, 2L, LocalDate.now().plusDays(2), "doublon"));
        verify(absenceRepository, never()).save(any());
    }

    @Test
    void declaredAbsenceCarriesMotifToCoach() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        Creneau creneau = Creneau.builder().jourSemaine(JourSemaine.LUNDI)
                .heureDebut(java.time.LocalTime.of(16, 0))
                .heureFin(java.time.LocalTime.of(17, 0)).build();
        creneau.setId(2L);
        when(creneauRepository.findById(2L)).thenReturn(Optional.of(creneau));
        when(absenceRepository.findByJoueurIdAndCreneauIdAndDateSeance(eq(1L), eq(2L), any()))
                .thenReturn(Optional.empty());
        when(absenceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AbsenceResponse response = absenceService.declareAbsence(
                1L, 2L, LocalDate.now().plusDays(3), "Match de basket");

        assertEquals("Match de basket", response.getMotif());
        assertEquals(2L, response.getCreneauId());
    }
}
