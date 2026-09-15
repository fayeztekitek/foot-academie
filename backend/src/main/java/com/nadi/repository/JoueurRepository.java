package com.nadi.repository;

import com.nadi.model.Joueur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoueurRepository extends JpaRepository<Joueur, Long> {

    Page<Joueur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    Page<Joueur> findByCategorieId(Long categorieId, Pageable pageable);

    List<Joueur> findByCategorieId(Long categorieId);

    Page<Joueur> findByParentId(Long parentId, Pageable pageable);

    List<Joueur> findByParentId(Long parentId);

    @Query("SELECT j FROM Joueur j LEFT JOIN FETCH j.categorie LEFT JOIN FETCH j.parent WHERE j.id = :id")
    Joueur findByIdWithDetails(@Param("id") Long id);

    long countByCategorieId(Long categorieId);

    long countByStatutPaiement(Joueur.StatutPaiement statut);

    long countByCertificatMedicalFalse();

    long countByAutorisationParentaleFalse();

    long countByTenantId(Long tenantId);
}
