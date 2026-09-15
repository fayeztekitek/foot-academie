package com.nadi.repository;

import com.nadi.model.ConsentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {

    List<ConsentLog> findByParentIdOrderByDateConsentementDesc(Long parentId);

    List<ConsentLog> findAllByOrderByDateConsentementDesc();

    @Query("SELECT c FROM ConsentLog c LEFT JOIN FETCH c.parent WHERE c.dateConsentement BETWEEN :start AND :end ORDER BY c.dateConsentement")
    List<ConsentLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c FROM ConsentLog c LEFT JOIN FETCH c.parent WHERE c.exported = false ORDER BY c.dateConsentement")
    List<ConsentLog> findUnexported();

    long countByParentId(Long parentId);

    @Query("SELECT c FROM ConsentLog c LEFT JOIN FETCH c.parent WHERE c.parent.id = :parentId AND c.type = :type ORDER BY c.dateConsentement DESC")
    List<ConsentLog> findByParentIdAndType(@Param("parentId") Long parentId, @Param("type") ConsentLog.ConsentType type);
}
