package com.nadi.service;

import com.nadi.dto.CreneauRequest;
import com.nadi.model.*;
import com.nadi.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreneauServiceTest {

    @Mock
    private CreneauRepository creneauRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private com.nadi.repository.CreneauExceptionRepository creneauExceptionRepository;

    private CreneauService service() {
        return new CreneauService(creneauRepository, categorieRepository, entraineurRepository,
                absenceRepository, joueurRepository, notificationService, creneauExceptionRepository);
    }

    private Utilisateur user(Long id, Role role) {
        return Utilisateur.builder()
                .id(id).email("u" + id + "@nadi.tn").motDePasseHash("hash")
                .role(role).actif(true).tenantId(1L).build();
    }

    private Entraineur coach(Long id, Utilisateur user) {
        Entraineur coach = Entraineur.builder().id(id).prenom("Karim").nom("Amri").build();
        coach.setUtilisateur(user);
        return coach;
    }

    private Joueur player(Long id, Parent parent) {
        Joueur joueur = Joueur.builder().prenom("J").nom("N")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(id);
        joueur.setParent(parent);
        return joueur;
    }

    private Parent parent(Long id, Utilisateur user) {
        Parent parent = Parent.builder().id(id).prenom("P").nom("N").email("p@nadi.tn").build();
        parent.setUtilisateur(user);
        return parent;
    }

    private Categorie category() {
        Categorie cat = Categorie.builder().nom("U13").build();
        cat.setId(4L);
        return cat;
    }

    private CreneauRequest request() {
        CreneauRequest request = new CreneauRequest();
        request.setJourSemaine("LUNDI");
        request.setHeureDebut(LocalTime.of(16, 0));
        request.setHeureFin(LocalTime.of(17, 30));
        request.setCategorieId(4L);
        request.setTerrain("Terrain A");
        request.setEntraineurId(5L);
        return request;
    }

    private Creneau creneau(Entraineur coach) {
        Creneau creneau = Creneau.builder()
                .jourSemaine(JourSemaine.LUNDI)
                .heureDebut(LocalTime.of(16, 0)).heureFin(LocalTime.of(17, 30))
                .categorie(category()).terrain("Terrain A").build();
        creneau.setId(1L);
        creneau.setEntraineurs(new java.util.HashSet<>(Set.of(coach)));
        return creneau;
    }

    @Test
    void createNotifiesCoachAndCategoryParents() {
        Entraineur coach = coach(5L, user(7L, Role.COACH));
        Utilisateur parentUser = user(42L, Role.PARENT);
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(creneauRepository.save(any())).thenAnswer(i -> {
            Creneau c = i.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(joueurRepository.findByCategorieId(4L))
                .thenReturn(List.of(player(1L, parent(10L, parentUser))));

        service().create(request());

        ArgumentCaptor<Utilisateur> users = ArgumentCaptor.forClass(Utilisateur.class);
        verify(notificationService, times(2)).create(users.capture(),
                eq(Notification.TypeNotification.CHANGEMENT_HORAIRE), anyString(), anyString());
        List<Long> notifiedIds = users.getAllValues().stream().map(Utilisateur::getId).toList();
        assertTrue(notifiedIds.containsAll(List.of(42L, 7L)));
    }

    @Test
    void notificationDetailsContainSchedule() {
        Entraineur coach = coach(5L, user(7L, Role.COACH));
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(creneauRepository.save(any())).thenAnswer(i -> {
            Creneau c = i.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(joueurRepository.findByCategorieId(4L)).thenReturn(List.of());

        service().create(request());

        ArgumentCaptor<String> details = ArgumentCaptor.forClass(String.class);
        verify(notificationService).create(any(), any(), anyString(), details.capture());
        assertTrue(details.getValue().contains("Lundi"));
        assertTrue(details.getValue().contains("16:00-17:30"));
        assertTrue(details.getValue().contains("Terrain A"));
    }

    @Test
    void skipsUsersWithoutAccounts() {
        Entraineur ghost = coach(5L, null);
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(ghost));
        when(creneauRepository.save(any())).thenAnswer(i -> {
            Creneau c = i.getArgument(0);
            c.setId(1L);
            return c;
        });
        Parent orphan = parent(10L, null);
        when(joueurRepository.findByCategorieId(4L))
                .thenReturn(List.of(player(1L, orphan)));

        service().create(request());

        verify(notificationService, never()).create(any(), any(), any(), any());
    }

    @Test
    void updateNotifiesOfChange() {
        Entraineur coach = coach(5L, user(7L, Role.COACH));
        Creneau existing = creneau(coach);
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categorieRepository.findById(4L)).thenReturn(Optional.of(category()));
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(creneauRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(joueurRepository.findByCategorieId(4L)).thenReturn(List.of());

        service().update(1L, request());

        verify(notificationService, atLeastOnce()).create(any(),
                eq(Notification.TypeNotification.CHANGEMENT_HORAIRE), anyString(), anyString());
    }

    private Creneau weeklySlot(java.time.LocalDate debut, java.time.LocalDate fin) {
        Creneau slot = Creneau.builder()
                .jourSemaine(JourSemaine.LUNDI)
                .heureDebut(LocalTime.of(16, 0)).heureFin(LocalTime.of(17, 30))
                .categorie(category()).terrain("Terrain A")
                .dateDebut(debut).dateFin(fin).build();
        slot.setId(1L);
        return slot;
    }

    private java.time.LocalDate nextMonday() {
        return java.time.LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY));
    }

    @Test
    void expandsWeeklyOccurrencesWithinBounds() {
        java.time.LocalDate monday = nextMonday();
        when(creneauRepository.findAll()).thenReturn(List.of(weeklySlot(monday, monday.plusDays(13))));
        when(creneauExceptionRepository.findByCreneauId(1L)).thenReturn(List.of());

        List<com.nadi.dto.CreneauOccurrenceResponse> occurrences =
                service().getOccurrences(monday.minusDays(30), monday.plusDays(60));

        // Bounded by the slot's own [dateDebut, dateFin]: exactly 2 Mondays.
        assertEquals(2, occurrences.size());
        assertTrue(occurrences.stream().allMatch(o -> "NORMALE".equals(o.getStatut())));
        assertEquals(monday, occurrences.get(0).getDate());
    }

    @Test
    void oneShotSlotYieldsSingleOccurrence() {
        java.time.LocalDate monday = nextMonday();
        when(creneauRepository.findAll()).thenReturn(List.of(weeklySlot(monday, monday)));
        when(creneauExceptionRepository.findByCreneauId(1L)).thenReturn(List.of());

        List<com.nadi.dto.CreneauOccurrenceResponse> occurrences =
                service().getOccurrences(monday.minusDays(30), monday.plusDays(60));

        assertEquals(1, occurrences.size());
    }

    @Test
    void cancelledOccurrenceCarriesMotif() {
        java.time.LocalDate monday = nextMonday();
        when(creneauRepository.findAll()).thenReturn(List.of(weeklySlot(monday, monday.plusDays(13))));
        when(creneauExceptionRepository.findByCreneauId(1L)).thenReturn(List.of(
                com.nadi.model.CreneauException.builder()
                        .creneau(weeklySlot(monday, monday.plusDays(13)))
                        .date(monday)
                        .statut(com.nadi.model.CreneauException.StatutException.ANNULEE)
                        .motif("Tournoi inter-académies")
                        .build()));

        List<com.nadi.dto.CreneauOccurrenceResponse> occurrences =
                service().getOccurrences(monday, monday.plusDays(13));

        assertEquals(2, occurrences.size());
        assertEquals("ANNULEE", occurrences.get(0).getStatut());
        assertEquals("Tournoi inter-académies", occurrences.get(0).getMotif());
        assertEquals("NORMALE", occurrences.get(1).getStatut());
    }

    @Test
    void saveExceptionRequiresMotif() {
        Creneau slot = weeklySlot(nextMonday(), null);
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(slot));

        assertThrows(RuntimeException.class, () ->
                service().saveException(1L, nextMonday(), "ANNULEE", null, null, null, "  "));
        assertThrows(RuntimeException.class, () ->
                service().saveException(1L, nextMonday(), "REPORTE", null, null, null, "x"));
        verify(creneauExceptionRepository, never()).save(any());
    }

    @Test
    void createRejectsInvertedDateRange() {
        CreneauRequest bad = request();
        bad.setDateDebut(java.time.LocalDate.now().plusDays(10));
        bad.setDateFin(java.time.LocalDate.now());

        assertThrows(RuntimeException.class, () -> service().create(bad));
        verify(creneauRepository, never()).save(any());
    }

    @Test
    void deleteNotifiesBeforeRemoval() {
        Entraineur coach = coach(5L, user(7L, Role.COACH));
        Creneau existing = creneau(coach);
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(joueurRepository.findByCategorieId(4L)).thenReturn(List.of());

        service().delete(1L);

        ArgumentCaptor<Utilisateur> users = ArgumentCaptor.forClass(Utilisateur.class);
        verify(notificationService).create(users.capture(), any(), anyString(), anyString());
        assertEquals(7L, users.getValue().getId());
        verify(creneauRepository).deleteById(1L);
    }
}
