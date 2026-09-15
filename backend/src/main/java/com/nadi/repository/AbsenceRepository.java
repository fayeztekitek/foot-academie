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

    @Modifying
    @Transactional
    void deleteByCreneauId(Long creneauId);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.joueur.id = :joueurId AND a.dateSeance BETWEEN :start AND :end")
    long countSessionsByJoueurAndDateRange(@Param("joueurId") Long joueurId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.joueur.id = :joueurId AND a.present = true AND a.dateSeance BETWEEN :start AND :end")
    long countPresentByJoueurAndDateRange(@Param("joueurId") Long joueurId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.joueur.id = :joueurId")
    long countAllSessionsByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.joueur.id = :joueurId AND a.present = true")
    long countAllPresentByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.dateSeance BETWEEN :start AND :end")
    long countAllSessionsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(DISTINCT a.dateSeance) FROM Absence a WHERE a.present = true AND a.dateSeance BETWEEN :start AND :end")
    long countAllPresentByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
