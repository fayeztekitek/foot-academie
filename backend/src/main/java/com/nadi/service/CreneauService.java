package com.nadi.service;

import com.nadi.dto.CreneauRequest;
import com.nadi.dto.CreneauResponse;
import com.nadi.model.Categorie;
import com.nadi.model.Creneau;
import com.nadi.model.Entraineur;
import com.nadi.model.JourSemaine;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.AbsenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreneauService {

    private final CreneauRepository creneauRepository;
    private final CategorieRepository categorieRepository;
    private final EntraineurRepository entraineurRepository;
    private final AbsenceRepository absenceRepository;

    @Transactional(readOnly = true)
    public List<CreneauResponse> getAll() {
        return creneauRepository.findAllWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreneauResponse> getByJour(String jour) {
        JourSemaine jourSemaine = JourSemaine.valueOf(jour.toUpperCase());
        return creneauRepository.findByJourSemaine(jourSemaine).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreneauResponse> getByCategorie(Long categorieId) {
        return creneauRepository.findByCategorieId(categorieId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreneauResponse> getByEntraineur(Long entraineurId) {
        return creneauRepository.findByEntraineurId(entraineurId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CreneauResponse create(CreneauRequest request) {
        JourSemaine jour = JourSemaine.valueOf(request.getJourSemaine().toUpperCase());

        validateNoOverlap(null, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());
        validateNoCoachOverlap(null, request.getEntraineurId(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));
        Entraineur entraineur = entraineurRepository.findById(request.getEntraineurId())
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + request.getEntraineurId()));

        Creneau creneau = Creneau.builder()
                .jourSemaine(jour)
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .categorie(categorie)
                .entraineur(entraineur)
                .terrain(request.getTerrain())
                .build();
        return toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public CreneauResponse update(Long id, CreneauRequest request) {
        Creneau creneau = creneauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + id));

        JourSemaine jour = JourSemaine.valueOf(request.getJourSemaine().toUpperCase());

        validateNoOverlap(id, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());
        validateNoCoachOverlap(id, request.getEntraineurId(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));
        Entraineur entraineur = entraineurRepository.findById(request.getEntraineurId())
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + request.getEntraineurId()));

        creneau.setJourSemaine(jour);
        creneau.setHeureDebut(request.getHeureDebut());
        creneau.setHeureFin(request.getHeureFin());
        creneau.setCategorie(categorie);
        creneau.setEntraineur(entraineur);
        creneau.setTerrain(request.getTerrain());
        return toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public void delete(Long id) {
        if (!creneauRepository.existsById(id)) {
            throw new RuntimeException("Créneau non trouvé: " + id);
        }
        absenceRepository.deleteByCreneauId(id);
        creneauRepository.deleteById(id);
    }

    private void validateNoOverlap(Long excludeId, String terrain, JourSemaine jour, LocalTime debut, LocalTime fin) {
        List<Creneau> overlapping;
        if (excludeId != null) {
            overlapping = creneauRepository.findOverlappingExcluding(terrain, jour, debut, fin, excludeId);
        } else {
            overlapping = creneauRepository.findOverlapping(terrain, jour, debut, fin);
        }
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Conflit horaire: ce terrain est déjà occupé à ce créneau");
        }
    }

    private void validateNoCoachOverlap(Long excludeId, Long entraineurId, JourSemaine jour, LocalTime debut, LocalTime fin) {
        List<Creneau> overlapping;
        if (excludeId != null) {
            overlapping = creneauRepository.findCoachOverlappingExcluding(entraineurId, jour, debut, fin, excludeId);
        } else {
            overlapping = creneauRepository.findCoachOverlapping(entraineurId, jour, debut, fin);
        }
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Conflit horaire: cet entraîneur est déjà occupé à ce créneau");
        }
    }

    private CreneauResponse toResponse(Creneau c) {
        return CreneauResponse.builder()
                .id(c.getId())
                .jourSemaine(c.getJourSemaine().name())
                .heureDebut(c.getHeureDebut())
                .heureFin(c.getHeureFin())
                .categorieId(c.getCategorie().getId())
                .categorieNom(c.getCategorie().getNom())
                .entraineurId(c.getEntraineur().getId())
                .entraineurNom(c.getEntraineur().getNom())
                .entraineurPrenom(c.getEntraineur().getPrenom())
                .terrain(c.getTerrain())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
