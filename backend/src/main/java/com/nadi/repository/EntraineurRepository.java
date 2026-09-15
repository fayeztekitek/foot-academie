package com.nadi.repository;

import com.nadi.model.Entraineur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntraineurRepository extends JpaRepository<Entraineur, Long> {

    Page<Entraineur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    @Query("SELECT e FROM Entraineur e LEFT JOIN FETCH e.categories WHERE e.id = :id")
    Optional<Entraineur> findByIdWithCategories(@Param("id") Long id);

    @Query("SELECT e FROM Entraineur e LEFT JOIN FETCH e.categories")
    List<Entraineur> findAllWithCategories();

    Optional<Entraineur> findByUtilisateurId(Long utilisateurId);

    boolean existsByCreneauxId(Long creneauId);

    long countByTenantId(Long tenantId);
}
