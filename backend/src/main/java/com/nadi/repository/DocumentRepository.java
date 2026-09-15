package com.nadi.repository;

import com.nadi.model.Document;
import com.nadi.model.StatutDocument;
import com.nadi.model.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByJoueurId(Long joueurId);

    List<Document> findByType(TypeDocument type);

    List<Document> findByStatut(StatutDocument statut);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.joueur WHERE d.dateExpiration BETWEEN :start AND :end")
    List<Document> findExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.joueur WHERE d.dateExpiration < :today AND d.statut <> 'EXPIRE'")
    List<Document> findExpiredNotMarked(@Param("today") LocalDate today);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.joueur WHERE d.joueur.parent.id = :parentId")
    List<Document> findByParentId(@Param("parentId") Long parentId);
}
