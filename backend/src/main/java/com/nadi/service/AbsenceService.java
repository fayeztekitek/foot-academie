package com.nadi.service;

import com.nadi.dto.AbsenceRequest;
import com.nadi.dto.AbsenceResponse;
import com.nadi.model.*;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.security.FamilyAccessGuard;
import com.nadi.security.SecurityUtils;
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
    private final ParentRepository parentRepository;
    private final FamilyAccessGuard familyAccessGuard;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Long resolveOwnParentId(Long userId) {
        return parentRepository.findByUtilisateurId(userId)
                .map(Parent::getId)
                .orElseThrow(() -> new RuntimeException("Profil parent introuvable"));
    }

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getByCreneauAndDate(Long creneauId, java.time.LocalDate dateSeance) {
        return absenceRepository.findByCreneauIdAndDateSeance(creneauId, dateSeance)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getUpcomingByParent(Long parentId) {
        return absenceRepository.findUpcomingByParent(parentId, java.time.LocalDate.now())
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

    /**
     * A parent declares a future absence for their own child (excused).
     * The coach still confirms on the day via the regular presence flow.
     */
    @Transactional
    public AbsenceResponse declareAbsence(Long joueurId, Long creneauId,
                                          java.time.LocalDate dateSeance, String motif) {
        if (dateSeance == null || dateSeance.isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("La date de la séance doit être aujourd'hui ou dans le futur");
        }
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));
        familyAccessGuard.requireAccessToJoueur(joueur);
        Creneau creneau = creneauRepository.findById(creneauId)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + creneauId));

        if (absenceRepository.findByJoueurIdAndCreneauIdAndDateSeance(joueurId, creneauId, dateSeance).isPresent()) {
            throw new RuntimeException("Une absence est déjà signalée pour cette séance");
        }

        Utilisateur declarant = securityUtils.getCurrentUserOrThrow();
        Absence absence = Absence.builder()
                .joueur(joueur)
                .creneau(creneau)
                .dateSeance(dateSeance)
                .present(false)
                .motif(motif)
                .marquePar(declarant)
                .build();
        return toResponse(absenceRepository.save(absence));
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
                .motif(a.getMotif())
                .marqueParEmail(a.getMarquePar() != null ? a.getMarquePar().getEmail() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
