package com.nadi.repository;

import com.nadi.model.CreneauException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreneauExceptionRepository extends JpaRepository<CreneauException, Long> {

    List<CreneauException> findByCreneauId(Long creneauId);

    Optional<CreneauException> findByCreneauIdAndDate(Long creneauId, LocalDate date);

    void deleteByCreneauIdAndDate(Long creneauId, LocalDate date);
}
