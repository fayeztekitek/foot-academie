package com.nadi.service;

import com.nadi.dto.NoteJoueurResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.security.FamilyAccessGuard;
import com.nadi.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteJoueurServiceTest {

    @Mock
    private NoteJoueurRepository noteJoueurRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private CreneauRepository creneauRepository;
    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private AttendanceService attendanceService;

    private NoteJoueurService noteJoueurService;

    @BeforeEach
    void setUp() {
        FamilyAccessGuard guard = new FamilyAccessGuard(
                parentRepository, new SecurityUtils(utilisateurRepository));
        noteJoueurService = new NoteJoueurService(
                noteJoueurRepository, joueurRepository, creneauRepository, entraineurRepository, guard,
                attendanceService);
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

    private NoteJoueur note(Joueur joueur) {
        Creneau creneau = Creneau.builder().jourSemaine(JourSemaine.LUNDI)
                .heureDebut(LocalTime.of(16, 0)).heureFin(LocalTime.of(17, 0)).build();
        creneau.setId(1L);
        Entraineur coach = Entraineur.builder().prenom("C").nom("N").build();
        coach.setId(1L);
        return NoteJoueur.builder().id(1L).joueur(joueur).creneau(creneau).entraineur(coach)
                .date(LocalDate.now()).physique(BigDecimal.valueOf(8)).technique(BigDecimal.valueOf(7))
                .explosivite(BigDecimal.valueOf(7)).tactique(BigDecimal.valueOf(6))
                .mental(BigDecimal.valueOf(8)).endurance(BigDecimal.valueOf(7))
                .noteGlobale(BigDecimal.valueOf(7.2)).build();
    }

    @Test
    void parentCanListOwnChildNotes() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(noteJoueurRepository.findByJoueurIdOrderByDateDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(note(joueur(10L)))));

        Page<NoteJoueurResponse> page = noteJoueurService.getByJoueur(1L, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void parentCannotListOtherChildNotes() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(11L)));

        assertThrows(AccessDeniedException.class,
                () -> noteJoueurService.getByJoueur(1L, PageRequest.of(0, 20)));
    }

    @Test
    void parentCannotReadOtherChildStats() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(11L)));

        assertThrows(AccessDeniedException.class, () -> noteJoueurService.getStats(1L));
    }

    @Test
    void coachCanCreateNote() {
        Utilisateur coach = Utilisateur.builder()
                .id(2L).email("c@nadi.tn").motDePasseHash("hash")
                .role(Role.COACH).actif(true).tenantId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(coach, null,
                        List.of(new SimpleGrantedAuthority("ROLE_COACH"))));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(
                Creneau.builder().jourSemaine(JourSemaine.LUNDI)
                        .heureDebut(LocalTime.of(16, 0)).heureFin(LocalTime.of(17, 0)).build()));
        when(entraineurRepository.findById(1L)).thenReturn(Optional.of(
                Entraineur.builder().prenom("C").nom("N").build()));
        when(noteJoueurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        com.nadi.dto.NoteJoueurRequest request = new com.nadi.dto.NoteJoueurRequest();
        request.setJoueurId(1L);
        request.setCreneauId(1L);
        request.setEntraineurId(1L);
        request.setPhysique(BigDecimal.valueOf(8));

        NoteJoueurResponse response = noteJoueurService.create(request);

        assertNotNull(response);
        verify(noteJoueurRepository).save(any());
    }

    @Test
    void createRejectsUnknownPlayer() {
        when(joueurRepository.findById(999L)).thenReturn(Optional.empty());

        com.nadi.dto.NoteJoueurRequest request = new com.nadi.dto.NoteJoueurRequest();
        request.setJoueurId(999L);
        request.setCreneauId(1L);
        request.setEntraineurId(1L);

        assertThrows(RuntimeException.class, () -> noteJoueurService.create(request));
    }

    private com.nadi.dto.PresenceStatsByJoueurResponse statsWithRate(double rate) {
        return com.nadi.dto.PresenceStatsByJoueurResponse.builder()
                .mois(com.nadi.dto.PresenceStatsResponse.builder().attendanceRate(rate).build())
                .build();
    }

    private Joueur rankedPlayer(Long id) {
        Joueur joueur = Joueur.builder().prenom("J" + id).nom("N")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(id);
        return joueur;
    }

    @Test
    void top10MixesNotesAndAttendance() {
        when(noteJoueurRepository.findBestJoueurByCategorieAndMonth(eq(4L), eq(9), eq(2026)))
                .thenReturn(List.of(
                        new Object[]{1L, BigDecimal.valueOf(8.5)},
                        new Object[]{2L, BigDecimal.valueOf(9.0)},
                        new Object[]{3L, BigDecimal.valueOf(7.0)}));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(rankedPlayer(1L)));
        when(joueurRepository.findById(2L)).thenReturn(Optional.of(rankedPlayer(2L)));
        when(joueurRepository.findById(3L)).thenReturn(Optional.of(rankedPlayer(3L)));
        when(attendanceService.getStatsByJoueur(1L)).thenReturn(statsWithRate(100.0));
        when(attendanceService.getStatsByJoueur(2L)).thenReturn(statsWithRate(50.0));
        when(attendanceService.getStatsByJoueur(3L)).thenReturn(statsWithRate(100.0));

        List<com.nadi.dto.TopJoueurResponse> top =
                noteJoueurService.getTop10ByCategorie(4L, 9, 2026);

        // p1: 59.5+30=89.5, p3: 49+30=79, p2: 63+15=78 — attendance flips p3 over p2
        assertEquals(List.of(1L, 3L, 2L), top.stream().map(com.nadi.dto.TopJoueurResponse::getJoueurId).toList());
        assertEquals(89.5, top.get(0).getScore());
    }

    @Test
    void top10IsLimitedToTen() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (long i = 1; i <= 12; i++) {
            rows.add(new Object[]{i, BigDecimal.valueOf(5 + i * 0.1)});
        }
        when(noteJoueurRepository.findBestJoueurByCategorieAndMonth(eq(4L), eq(9), eq(2026)))
                .thenReturn(rows);
        when(joueurRepository.findById(any()))
                .thenAnswer(i -> Optional.of(rankedPlayer((Long) i.getArgument(0))));
        when(attendanceService.getStatsByJoueur(any())).thenReturn(statsWithRate(100.0));

        List<com.nadi.dto.TopJoueurResponse> top =
                noteJoueurService.getTop10ByCategorie(4L, 9, 2026);

        assertEquals(10, top.size());
    }

    @Test
    void statsAggregateOwnChildNotes() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(noteJoueurRepository.findAverageNoteByJoueur(1L))
                .thenReturn(java.math.BigDecimal.valueOf(7.5));
        when(noteJoueurRepository.findByJoueurIdOrderByDateDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        Map<String, Object> stats = noteJoueurService.getStats(1L);

        assertEquals(java.math.BigDecimal.valueOf(7.5), stats.get("moyenneGlobale"));
    }
}
