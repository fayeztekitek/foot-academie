package com.nadi.repository;

import com.nadi.model.ConsentementRGPD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentementRGPDRepository extends JpaRepository<ConsentementRGPD, Long> {

    List<ConsentementRGPD> findByParentId(Long parentId);

    List<ConsentementRGPD> findAllByOrderByDateConsentementDesc();

    List<ConsentementRGPD> findByExporteFalse();
}
