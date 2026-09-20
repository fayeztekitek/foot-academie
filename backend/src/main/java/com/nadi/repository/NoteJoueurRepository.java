package com.nadi.repository;

import com.nadi.model.NoteJoueur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NoteJoueurRepository extends JpaRepository<NoteJoueur, Long> {

    Page<NoteJoueur> findByJoueurIdOrderByDateDesc(Long joueurId, Pageable pageable);

    List<NoteJoueur> findByJoueurIdAndCreneauId(Long joueurId, Long creneauId);

    @Query("SELECT AVG(n.noteGlobale) FROM NoteJoueur n WHERE n.joueur.id = :joueurId")
    BigDecimal findAverageNoteByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT AVG(n.physique) FROM NoteJoueur n WHERE n.joueur.id = :joueurId")
    BigDecimal findAveragePhysiqueByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT AVG(n.technique) FROM NoteJoueur n WHERE n.joueur.id = :joueurId")
    BigDecimal findAverageTechniqueByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT AVG(n.explosivite) FROM NoteJoueur n WHERE n.joueur.id = :joueurId")
    BigDecimal findAverageExplosiviteByJoueur(@Param("joueurId") Long joueurId);

    @Query("SELECT AVG(n.noteGlobale) FROM NoteJoueur n WHERE n.joueur.id = :joueurId AND MONTH(n.date) = :mois AND YEAR(n.date) = :annee")
    BigDecimal findAverageByJoueurAndMonth(@Param("joueurId") Long joueurId, @Param("mois") int mois, @Param("annee") int annee);

    @Query("SELECT n.joueur.id, AVG(n.noteGlobale) as avg FROM NoteJoueur n WHERE n.joueur.categorie.id = :categorieId AND MONTH(n.date) = :mois AND YEAR(n.date) = :annee GROUP BY n.joueur.id ORDER BY avg DESC")
    List<Object[]> findBestJoueurByCategorieAndMonth(@Param("categorieId") Long categorieId, @Param("mois") int mois, @Param("annee") int annee);
}
