package com.nadi.repository;

import com.nadi.model.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    Optional<Abonnement> findByTenantId(Long tenantId);

    Optional<Abonnement> findByTenantIdAndStatut(Long tenantId, Abonnement.StatutAbonnement statut);
}
