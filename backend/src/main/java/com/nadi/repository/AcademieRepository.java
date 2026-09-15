package com.nadi.repository;

import com.nadi.model.Academie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademieRepository extends JpaRepository<Academie, Long> {

    Optional<Academie> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
