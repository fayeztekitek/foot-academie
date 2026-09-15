package com.nadi.repository;

import com.nadi.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByTenantIdOrderByDateEmissionDesc(Long tenantId);

    List<Facture> findByTenantIdAndStatut(Long tenantId, Facture.StatutFacture statut);
}
