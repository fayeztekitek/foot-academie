package com.nadi.repository;

import com.nadi.model.Convocation;
import com.nadi.model.Evenement;
import com.nadi.model.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConvocationRepository extends JpaRepository<Convocation, Long> {

    List<Convocation> findByEvenement(Evenement evenement);

    List<Convocation> findByJoueurId(Long joueurId);

    Optional<Convocation> findByEvenementAndJoueur(Evenement evenement, Joueur joueur);

    boolean existsByEvenementAndJoueur(Evenement evenement, Joueur joueur);

    @Query("SELECT c FROM Convocation c LEFT JOIN FETCH c.joueur LEFT JOIN FETCH c.parent LEFT JOIN FETCH c.evenement WHERE c.parent.utilisateur.id = :userId ORDER BY c.evenement.dateDebut DESC")
    List<Convocation> findByParentUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Convocation c LEFT JOIN FETCH c.joueur LEFT JOIN FETCH c.parent LEFT JOIN FETCH c.evenement WHERE c.evenement.id = :evenementId")
    List<Convocation> findByEvenementId(@Param("evenementId") Long evenementId);
}
