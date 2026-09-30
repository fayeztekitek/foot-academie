package com.nadi.repository;

import com.nadi.model.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    List<Absence> findByCreneauIdAndDateSeance(Long creneauId, LocalDate dateSeance);

    Optional<Absence> findByJoueurIdAndCreneauIdAndDateSeance(Long joueurId, Long creneauId, LocalDate dateSeance);

    List<Absence> findByJoueurId(Long joueurId);

    @Query("SELECT a FROM Absence a LEFT JOIN FETCH a.joueur LEFT JOIN FETCH a.creneau WHERE a.joueur.parent.id = :parentId AND a.dateSeance >= :from ORDER BY a.dateSeance")
    List<Absence> findUpcomingByParent(@Param("parentId") Long parentId, @Param("from") LocalDate from);

    @Modifying
    @Transactional
    void deleteByCreneauId(Long creneauId);

    // NOTE: count ROWS, not DISTINCT dates. Each row is one recorded
    // player-session (unique per joueur/creneau/date). Counting distinct
    // dates globally made a single present mark turn the whole date
    // "present", pinning dashboard rates at ~100%.
    @Query("SELECT COUNT(a) FROM Absence a WHERE a.joueur.id = :joueurId AND a.dateSeance BETWEEN :start AND :end")
    long countSessionsByJoueurAndDateRange(@Param("joueurId") Long joueurId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.joueur.id = :joueurId AND a.present = true AND a.dateSeance BETWEEN :start AND :end")
    long countPresentByJoueurAndDateRange(@Param("joueurId") Long joueurId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.joueur.id = :joueurId")
    long countAllSessionsByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.joueur.id = :joueurId AND a.present = true")
    long countAllPresentByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.dateSeance BETWEEN :start AND :end")
    long countAllSessionsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Absence a WHERE a.present = true AND a.dateSeance BETWEEN :start AND :end")
    long countAllPresentByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
