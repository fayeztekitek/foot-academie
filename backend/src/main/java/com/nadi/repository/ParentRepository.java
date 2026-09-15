package com.nadi.repository;

import com.nadi.model.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    Page<Parent> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    Optional<Parent> findByUtilisateurId(Long utilisateurId);

    @Query("SELECT p FROM Parent p LEFT JOIN FETCH p.joueurs WHERE p.id = :id")
    Optional<Parent> findByIdWithJoueurs(@Param("id") Long id);

    List<Parent> findByEmail(String email);

    long countByTenantId(Long tenantId);
}
