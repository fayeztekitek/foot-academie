package com.nadi.repository;

import com.nadi.model.Categorie;
import com.nadi.model.Creneau;
import com.nadi.model.JourSemaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, Long> {

    List<Creneau> findByJourSemaine(JourSemaine jourSemaine);

    @Query("SELECT c FROM Creneau c LEFT JOIN FETCH c.categorie LEFT JOIN FETCH c.entraineur ORDER BY c.jourSemaine, c.heureDebut")
    List<Creneau> findAllWithDetails();

    @Query("SELECT c FROM Creneau c WHERE c.terrain = :terrain AND c.jourSemaine = :jour AND c.heureDebut < :fin AND c.heureFin > :debut")
    List<Creneau> findOverlapping(@Param("terrain") String terrain,
                                   @Param("jour") JourSemaine jour,
                                   @Param("debut") LocalTime debut,
                                   @Param("fin") LocalTime fin);

    @Query("SELECT c FROM Creneau c WHERE c.entraineur.id = :entraineurId AND c.jourSemaine = :jour AND c.heureDebut < :fin AND c.heureFin > :debut")
    List<Creneau> findCoachOverlapping(@Param("entraineurId") Long entraineurId,
                                        @Param("jour") JourSemaine jour,
                                        @Param("debut") LocalTime debut,
                                        @Param("fin") LocalTime fin);

    @Query("SELECT c FROM Creneau c JOIN c.entraineurs e WHERE e.id = :entraineurId AND c.jourSemaine = :jour AND c.heureDebut < :fin AND c.heureFin > :debut")
    List<Creneau> findCoachMultipleOverlapping(@Param("entraineurId") Long entraineurId,
                                               @Param("jour") JourSemaine jour,
                                               @Param("debut") LocalTime debut,
                                               @Param("fin") LocalTime fin);

    @Query("SELECT c FROM Creneau c WHERE c.terrain = :terrain AND c.jourSemaine = :jour AND c.heureDebut < :fin AND c.heureFin > :debut AND c.id <> :excludeId")
    List<Creneau> findOverlappingExcluding(@Param("terrain") String terrain,
                                            @Param("jour") JourSemaine jour,
                                            @Param("debut") LocalTime debut,
                                            @Param("fin") LocalTime fin,
                                            @Param("excludeId") Long excludeId);

    @Query("SELECT c FROM Creneau c WHERE c.entraineur.id = :entraineurId AND c.jourSemaine = :jour AND c.heureDebut < :fin AND c.heureFin > :debut AND c.id <> :excludeId")
    List<Creneau> findCoachOverlappingExcluding(@Param("entraineurId") Long entraineurId,
                                                 @Param("jour") JourSemaine jour,
                                                 @Param("debut") LocalTime debut,
                                                 @Param("fin") LocalTime fin,
                                                 @Param("excludeId") Long excludeId);

    List<Creneau> findByCategorieId(Long categorieId);

    List<Creneau> findByEntraineurId(Long entraineurId);

    @Query("SELECT c FROM Creneau c JOIN c.entraineurs e WHERE e.id = :entraineurId")
    List<Creneau> findByEntraineursId(@Param("entraineurId") Long entraineurId);

    @Query("SELECT DISTINCT c.categorie FROM Creneau c WHERE c.tenantId = :tenantId")
    List<Categorie> findDistinctCategories(@Param("tenantId") Long tenantId);
}
