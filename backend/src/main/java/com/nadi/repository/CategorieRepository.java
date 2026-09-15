package com.nadi.repository;

import com.nadi.model.Categorie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    List<Categorie> findByNomContainingIgnoreCase(String nom);

    Page<Categorie> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    @Query("SELECT c FROM Categorie c LEFT JOIN FETCH c.joueurs")
    List<Categorie> findAllWithJoueurs();

    boolean existsByNom(String nom);
}
