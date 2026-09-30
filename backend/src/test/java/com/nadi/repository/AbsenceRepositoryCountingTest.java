package com.nadi.repository;

import com.nadi.model.*;
import com.nadi.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves presence rates count recorded player-sessions (rows), not distinct
 * dates. Counting distinct dates pinned global rates at ~100%: a single
 * present mark turned the whole date "present".
 */
@DataJpaTest
class AbsenceRepositoryCountingTest {

    @Autowired
    private AbsenceRepository absenceRepository;

    @Autowired
    private TestEntityManager entities;

    private Joueur Ghita;
    private Joueur karim;
    private Creneau creneau;
    private final LocalDate day = LocalDate.now().minusDays(2);

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
        Categorie categorie = entities.persist(Categorie.builder()
                .nom("U13").ageMin(12).ageMax(13).tenantId(1L).build());
        Ghita = entities.persist(Joueur.builder()
                .prenom("Ghita").nom("N").dateNaissance(LocalDate.of(2015, 1, 1)).build());
        karim = entities.persist(Joueur.builder()
                .prenom("Karim").nom("N").dateNaissance(LocalDate.of(2015, 1, 1)).build());
        creneau = entities.persist(Creneau.builder()
                .jourSemaine(JourSemaine.LUNDI)
                .heureDebut(LocalTime.of(16, 0)).heureFin(LocalTime.of(17, 0))
                .categorie(categorie).terrain("Terrain A").build());
        entities.flush();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private void mark(Joueur joueur, boolean present) {
        Absence absence = Absence.builder()
                .joueur(joueur).creneau(creneau).dateSeance(day).present(present).build();
        entities.persist(absence);
        entities.flush();
    }

    @Test
    void globalRateCountsRowsNotDates() {
        mark(Ghita, true);
        mark(karim, false);

        long total = absenceRepository.countAllSessionsByDateRange(day.minusDays(1), day.plusDays(1));
        long present = absenceRepository.countAllPresentByDateRange(day.minusDays(1), day.plusDays(1));

        assertEquals(2, total);
        assertEquals(1, present);
    }

    @Test
    void twoSlotsSameDayCountAsTwoSessions() {
        Creneau evening = entities.persist(Creneau.builder()
                .jourSemaine(JourSemaine.LUNDI)
                .heureDebut(LocalTime.of(18, 0)).heureFin(LocalTime.of(19, 0))
                .categorie(creneau.getCategorie()).terrain("Terrain B").build());
        entities.flush();

        entities.persist(Absence.builder().joueur(Ghita).creneau(creneau).dateSeance(day).present(true).build());
        entities.persist(Absence.builder().joueur(Ghita).creneau(evening).dateSeance(day).present(false).build());
        entities.flush();

        long total = absenceRepository.countSessionsByJoueurAndDateRange(
                Ghita.getId(), day.minusDays(1), day.plusDays(1));
        long present = absenceRepository.countPresentByJoueurAndDateRange(
                Ghita.getId(), day.minusDays(1), day.plusDays(1));

        assertEquals(2, total);
        assertEquals(1, present);
    }

    @Test
    void playerWithoutRowsContributesNothing() {
        mark(Ghita, true);

        long total = absenceRepository.countSessionsByJoueurAndDateRange(
                karim.getId(), day.minusDays(1), day.plusDays(1));

        assertEquals(0, total);
    }
}
