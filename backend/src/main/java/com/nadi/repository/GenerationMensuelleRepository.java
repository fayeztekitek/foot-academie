package com.nadi.repository;

import com.nadi.model.GenerationMensuelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationMensuelleRepository extends JpaRepository<GenerationMensuelle, Long> {

    boolean existsByTenantIdAndMois(Long tenantId, String mois);
}
