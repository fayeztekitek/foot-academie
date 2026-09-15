package com.nadi.service;

import com.nadi.dto.PresenceStatsByJoueurResponse;
import com.nadi.dto.PresenceStatsGlobalResponse;
import com.nadi.dto.PresenceStatsResponse;
import com.nadi.model.Joueur;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.JoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AbsenceRepository absenceRepository;
    private final JoueurRepository joueurRepository;

    @Transactional(readOnly = true)
    public PresenceStatsGlobalResponse getGlobalStats() {
        LocalDate today = LocalDate.now();
        LocalDate debutMois = today.withDayOfMonth(1);
        LocalDate debutTrimestre = today.withMonth(((today.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        LocalDate debutSemestre = today.getMonthValue() <= 6
                ? LocalDate.of(today.getYear(), 1, 1)
                : LocalDate.of(today.getYear(), 7, 1);
        LocalDate debutAnnee = LocalDate.of(today.getYear(), 1, 1);

        return PresenceStatsGlobalResponse.builder()
                .mois(computeGlobalStats(debutMois, today))
                .trimestre(computeGlobalStats(debutTrimestre, today))
                .semestre(computeGlobalStats(debutSemestre, today))
                .annee(computeGlobalStats(debutAnnee, today))
                .build();
    }

    @Transactional(readOnly = true)
    public PresenceStatsByJoueurResponse getStatsByJoueur(Long joueurId) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));

        LocalDate today = LocalDate.now();
        LocalDate debutMois = today.withDayOfMonth(1);
        LocalDate debutTrimestre = today.withMonth(((today.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        LocalDate debutSemestre = today.getMonthValue() <= 6
                ? LocalDate.of(today.getYear(), 1, 1)
                : LocalDate.of(today.getYear(), 7, 1);
        LocalDate debutAnnee = LocalDate.of(today.getYear(), 1, 1);

        return PresenceStatsByJoueurResponse.builder()
                .joueurId(joueur.getId())
                .joueurPrenom(joueur.getPrenom())
                .joueurNom(joueur.getNom())
                .mois(computeJoueurStats(joueurId, debutMois, today))
                .trimestre(computeJoueurStats(joueurId, debutTrimestre, today))
                .semestre(computeJoueurStats(joueurId, debutSemestre, today))
                .annee(computeJoueurStats(joueurId, debutAnnee, today))
                .allTime(computeJoueurStatsAllTime(joueurId))
                .build();
    }

    @Transactional(readOnly = true)
    public List<PresenceStatsByJoueurResponse> getStatsByParent(Long parentId) {
        List<Joueur> joueurs = joueurRepository.findByParentId(parentId);
        return buildStatsForJoueurs(joueurs);
    }

    @Transactional(readOnly = true)
    public List<PresenceStatsByJoueurResponse> getAllJoueurStats() {
        List<Joueur> joueurs = joueurRepository.findAll();
        return buildStatsForJoueurs(joueurs);
    }

    private List<PresenceStatsByJoueurResponse> buildStatsForJoueurs(List<Joueur> joueurs) {
        LocalDate today = LocalDate.now();
        LocalDate debutMois = today.withDayOfMonth(1);
        LocalDate debutTrimestre = today.withMonth(((today.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        LocalDate debutSemestre = today.getMonthValue() <= 6
                ? LocalDate.of(today.getYear(), 1, 1)
                : LocalDate.of(today.getYear(), 7, 1);
        LocalDate debutAnnee = LocalDate.of(today.getYear(), 1, 1);

        return joueurs.stream()
                .map(j -> PresenceStatsByJoueurResponse.builder()
                        .joueurId(j.getId())
                        .joueurPrenom(j.getPrenom())
                        .joueurNom(j.getNom())
                        .mois(computeJoueurStats(j.getId(), debutMois, today))
                        .trimestre(computeJoueurStats(j.getId(), debutTrimestre, today))
                        .semestre(computeJoueurStats(j.getId(), debutSemestre, today))
                        .annee(computeJoueurStats(j.getId(), debutAnnee, today))
                        .allTime(computeJoueurStatsAllTime(j.getId()))
                        .build())
                .toList();
    }

    private PresenceStatsResponse computeGlobalStats(LocalDate start, LocalDate end) {
        long total = absenceRepository.countAllSessionsByDateRange(start, end);
        long present = absenceRepository.countAllPresentByDateRange(start, end);
        return buildStats(total, present);
    }

    private PresenceStatsResponse computeJoueurStats(Long joueurId, LocalDate start, LocalDate end) {
        long total = absenceRepository.countSessionsByJoueurAndDateRange(joueurId, start, end);
        long present = absenceRepository.countPresentByJoueurAndDateRange(joueurId, start, end);
        return buildStats(total, present);
    }

    private PresenceStatsResponse computeJoueurStatsAllTime(Long joueurId) {
        long total = absenceRepository.countAllSessionsByJoueur(joueurId);
        long present = absenceRepository.countAllPresentByJoueur(joueurId);
        return buildStats(total, present);
    }

    private PresenceStatsResponse buildStats(long total, long present) {
        long absent = total - present;
        double rate = total > 0 ? Math.round((double) present / total * 10000.0) / 100.0 : 0.0;
        return PresenceStatsResponse.builder()
                .totalSessions(total)
                .presentCount(present)
                .absentCount(absent)
                .attendanceRate(rate)
                .build();
    }
}
