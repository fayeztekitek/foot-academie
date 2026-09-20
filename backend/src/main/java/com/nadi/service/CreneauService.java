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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        List<Creneau> creneauxSingle = creneauRepository.findByEntraineurId(entraineurId);
        List<Creneau> creneauxMultiple = creneauRepository.findByEntraineursId(entraineurId);
        
        java.util.Set<Long> ids = new java.util.HashSet<>();
        java.util.List<Creneau> all = new java.util.ArrayList<>();
        
        for (Creneau c : creneauxSingle) {
            if (ids.add(c.getId())) {
                all.add(c);
            }
        }
        for (Creneau c : creneauxMultiple) {
            if (ids.add(c.getId())) {
                all.add(c);
            }
        }
        
        return all.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CreneauResponse create(CreneauRequest request) {
        JourSemaine jour = JourSemaine.valueOf(request.getJourSemaine().toUpperCase());

        validateNoOverlap(null, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));

        Creneau creneau = Creneau.builder()
                .jourSemaine(jour)
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .categorie(categorie)
                .terrain(request.getTerrain())
                .build();

        Set<Entraineur> entraineurs = new HashSet<>();
        if (request.getEntraineurIds() != null && !request.getEntraineurIds().isEmpty()) {
            for (Long entraineurId : request.getEntraineurIds()) {
                Entraineur entraineur = entraineurRepository.findById(entraineurId)
                        .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + entraineurId));
                entraineurs.add(entraineur);
            }
            creneau.setEntraineurs(entraineurs);
        } else if (request.getEntraineurId() != null) {
            Entraineur entraineur = entraineurRepository.findById(request.getEntraineurId())
                    .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + request.getEntraineurId()));
            entraineurs.add(entraineur);
            creneau.setEntraineurs(entraineurs);
        }

        return toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public CreneauResponse update(Long id, CreneauRequest request) {
        Creneau creneau = creneauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + id));

        JourSemaine jour = JourSemaine.valueOf(request.getJourSemaine().toUpperCase());

        validateNoOverlap(id, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));

        creneau.setJourSemaine(jour);
        creneau.setHeureDebut(request.getHeureDebut());
        creneau.setHeureFin(request.getHeureFin());
        creneau.setCategorie(categorie);
        creneau.setTerrain(request.getTerrain());

        Set<Entraineur> entraineurs = new HashSet<>();
        if (request.getEntraineurIds() != null && !request.getEntraineurIds().isEmpty()) {
            for (Long entraineurId : request.getEntraineurIds()) {
                Entraineur entraineur = entraineurRepository.findById(entraineurId)
                        .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + entraineurId));
                entraineurs.add(entraineur);
            }
        } else if (request.getEntraineurId() != null) {
            Entraineur entraineur = entraineurRepository.findById(request.getEntraineurId())
                    .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + request.getEntraineurId()));
            entraineurs.add(entraineur);
        }
        creneau.setEntraineurs(entraineurs);

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
        List<CreneauResponse.EntraineurInfo> entraineurInfos = c.getEntraineurs() != null ?
                c.getEntraineurs().stream()
                        .map(e -> CreneauResponse.EntraineurInfo.builder()
                                .id(e.getId())
                                .nom(e.getNom())
                                .prenom(e.getPrenom())
                                .build())
                        .collect(Collectors.toList()) :
                (c.getEntraineur() != null ?
                        List.of(CreneauResponse.EntraineurInfo.builder()
                                .id(c.getEntraineur().getId())
                                .nom(c.getEntraineur().getNom())
                                .prenom(c.getEntraineur().getPrenom())
                                .build()) :
                        List.of());

        Long premierEntraineurId = null;
        String premierEntraineurNom = null;
        String premierEntraineurPrenom = null;
        if (!entraineurInfos.isEmpty()) {
            premierEntraineurId = entraineurInfos.get(0).getId();
            premierEntraineurNom = entraineurInfos.get(0).getNom();
            premierEntraineurPrenom = entraineurInfos.get(0).getPrenom();
        }

        return CreneauResponse.builder()
                .id(c.getId())
                .jourSemaine(c.getJourSemaine().name())
                .heureDebut(c.getHeureDebut())
                .heureFin(c.getHeureFin())
                .categorieId(c.getCategorie().getId())
                .categorieNom(c.getCategorie().getNom())
                .entraineurId(premierEntraineurId)
                .entraineurNom(premierEntraineurNom)
                .entraineurPrenom(premierEntraineurPrenom)
                .entraineurs(entraineurInfos)
                .terrain(c.getTerrain())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
