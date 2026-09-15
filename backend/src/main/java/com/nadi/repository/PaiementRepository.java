package com.nadi.repository;

import com.nadi.model.MoyenPaiement;
import com.nadi.model.Paiement;
import com.nadi.model.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    Page<Paiement> findByStatut(StatutPaiement statut, Pageable pageable);

    Page<Paiement> findByJoueurId(Long joueurId, Pageable pageable);

    List<Paiement> findByJoueurId(Long joueurId);

    List<Paiement> findByJoueurIdAndDateEcheanceBetween(Long joueurId, LocalDate start, LocalDate end);

    List<Paiement> findByDateEcheanceBeforeAndStatut(LocalDate date, StatutPaiement statut);

    Page<Paiement> findByParentId(Long parentId, Pageable pageable);

    List<Paiement> findByJoueurIdAndStatut(Long joueurId, StatutPaiement statut);

    @Query("SELECT p FROM Paiement p LEFT JOIN FETCH p.joueur LEFT JOIN FETCH p.parent ORDER BY p.dateEcheance DESC")
    Page<Paiement> findAllWithDetails(Pageable pageable);

    @Query("SELECT p FROM Paiement p LEFT JOIN FETCH p.joueur LEFT JOIN FETCH p.parent WHERE p.statut = :statut ORDER BY p.dateEcheance DESC")
    Page<Paiement> findByStatutWithDetails(@Param("statut") StatutPaiement statut, Pageable pageable);

    @Query("SELECT p FROM Paiement p LEFT JOIN FETCH p.joueur LEFT JOIN FETCH p.parent WHERE p.dateEcheance BETWEEN :start AND :end ORDER BY p.dateEcheance")
    List<Paiement> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT p FROM Paiement p LEFT JOIN FETCH p.joueur LEFT JOIN FETCH p.parent WHERE p.dateEcheance < :today AND p.statut = 'EN_ATTENTE'")
    List<Paiement> findOverdue(@Param("today") LocalDate today);

    long countByStatut(StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.statut = 'PAYE' AND p.datePaiement BETWEEN :start AND :end")
    java.math.BigDecimal sumPaidBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE p.statut IN ('EN_ATTENTE', 'EN_RETARD') AND p.dateEcheance BETWEEN :start AND :end")
    java.math.BigDecimal sumPendingBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Paiement> findByJoueurIdAndStatutAndDateEcheanceAfter(Long joueurId, StatutPaiement statut, LocalDate date);

    @Query("SELECT DISTINCT p.parent.id FROM Paiement p WHERE p.statut IN ('EN_ATTENTE', 'EN_RETARD') AND p.dateEcheance < :cutoff")
    List<Long> findParentIdsWithUnpaidOlderThan(@Param("cutoff") LocalDate cutoff);
}
