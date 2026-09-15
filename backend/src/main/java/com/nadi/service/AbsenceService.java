package com.nadi.service;

import com.nadi.dto.AbsenceRequest;
import com.nadi.dto.AbsenceResponse;
import com.nadi.model.*;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.JoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final CreneauRepository creneauRepository;
    private final JoueurRepository joueurRepository;

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getByCreneauAndDate(Long creneauId, java.time.LocalDate dateSeance) {
        return absenceRepository.findByCreneauIdAndDateSeance(creneauId, dateSeance)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<AbsenceResponse> savePresences(AbsenceRequest request, Utilisateur marquePar) {
        Creneau creneau = creneauRepository.findById(request.getCreneauId())
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + request.getCreneauId()));

        List<AbsenceResponse> results = new ArrayList<>();

        for (AbsenceRequest.PresenceItem item : request.getPresences()) {
            Joueur joueur = joueurRepository.findById(item.getJoueurId())
                    .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + item.getJoueurId()));

            Absence absence = absenceRepository
                    .findByJoueurIdAndCreneauIdAndDateSeance(item.getJoueurId(), request.getCreneauId(), request.getDateSeance())
                    .orElse(Absence.builder()
                            .joueur(joueur)
                            .creneau(creneau)
                            .dateSeance(request.getDateSeance())
                            .build());

            absence.setPresent(item.getPresent());
            absence.setMarquePar(marquePar);
            results.add(toResponse(absenceRepository.save(absence)));
        }

        return results;
    }

    private AbsenceResponse toResponse(Absence a) {
        return AbsenceResponse.builder()
                .id(a.getId())
                .joueurId(a.getJoueur().getId())
                .joueurPrenom(a.getJoueur().getPrenom())
                .joueurNom(a.getJoueur().getNom())
                .creneauId(a.getCreneau().getId())
                .categorieNom(a.getCreneau().getCategorie() != null ? a.getCreneau().getCategorie().getNom() : null)
                .dateSeance(a.getDateSeance())
                .present(a.getPresent())
                .marqueParEmail(a.getMarquePar() != null ? a.getMarquePar().getEmail() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
