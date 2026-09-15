package com.nadi.repository;

import com.nadi.model.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    List<Evenement> findByDateDebutBetween(LocalDate start, LocalDate end);

    @Query("SELECT e FROM Evenement e ORDER BY e.dateDebut DESC")
    List<Evenement> findAllOrdered();

    List<Evenement> findByTypeEvenement(String type);
}
