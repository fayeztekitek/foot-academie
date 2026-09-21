package com.nadi.service;

import com.nadi.dto.NoteJoueurRequest;
import com.nadi.dto.NoteJoueurResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoteJoueurService {

    private final NoteJoueurRepository noteJoueurRepository;
    private final JoueurRepository joueurRepository;
    private final CreneauRepository creneauRepository;
    private final EntraineurRepository entraineurRepository;

    @Transactional(readOnly = true)
    public Page<NoteJoueurResponse> getByJoueur(Long joueurId, Pageable pageable) {
        return noteJoueurRepository.findByJoueurIdOrderByDateDesc(joueurId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public NoteJoueurResponse create(NoteJoueurRequest request) {
        Joueur joueur = joueurRepository.findById(request.getJoueurId())
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + request.getJoueurId()));
        Creneau creneau = creneauRepository.findById(request.getCreneauId())
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + request.getCreneauId()));
        Entraineur entraineur = entraineurRepository.findById(request.getEntraineurId())
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + request.getEntraineurId()));

        NoteJoueur note = NoteJoueur.builder()
                .joueur(joueur)
                .creneau(creneau)
                .entraineur(entraineur)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .physique(request.getPhysique())
                .technique(request.getTechnique())
                .explosivite(request.getExplosivite())
                .tactique(request.getTactique() != null ? request.getTactique() : request.getPhysique())
                .mental(request.getMental() != null ? request.getMental() : request.getTechnique())
                .endurance(request.getEndurance() != null ? request.getEndurance() : request.getPhysique())
                .tenantId(TenantContext.getTenantId())
                .build();

        note.calculerNoteGlobale();
        return toResponse(noteJoueurRepository.save(note));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(Long joueurId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("moyenneGlobale", noteJoueurRepository.findAverageNoteByJoueur(joueurId));
        stats.put("moyennePhysique", noteJoueurRepository.findAveragePhysiqueByJoueur(joueurId));
        stats.put("moyenneTechnique", noteJoueurRepository.findAverageTechniqueByJoueur(joueurId));
        stats.put("moyenneExplosivite", noteJoueurRepository.findAverageExplosiviteByJoueur(joueurId));
        stats.put("moyenneTactique", noteJoueurRepository.findAverageTactiqueByJoueur(joueurId));
        stats.put("moyenneMental", noteJoueurRepository.findAverageMentalByJoueur(joueurId));
        stats.put("moyenneEndurance", noteJoueurRepository.findAverageEnduranceByJoueur(joueurId));

        LocalDate now = LocalDate.now();
        stats.put("moyenneMois", noteJoueurRepository.findAverageByJoueurAndMonth(joueurId, now.getMonthValue(), now.getYear()));
        stats.put("totalNotes", noteJoueurRepository.findByJoueurIdOrderByDateDesc(joueurId, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getJoueursDuMois(int mois, int annee) {
        List<Categorie> categories = creneauRepository.findDistinctCategories(TenantContext.getTenantId());
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Categorie cat : categories) {
            List<Object[]> best = noteJoueurRepository.findBestJoueurByCategorieAndMonth(cat.getId(), mois, annee);
            if (!best.isEmpty()) {
                Object[] top = best.get(0);
                Joueur joueur = joueurRepository.findById((Long) top[0]).orElse(null);
                if (joueur != null) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("categorieId", cat.getId());
                    entry.put("categorieNom", cat.getNom());
                    entry.put("joueurId", joueur.getId());
                    entry.put("joueurPrenom", joueur.getPrenom());
                    entry.put("joueurNom", joueur.getNom());
                    entry.put("noteMoyenne", top[1]);
                    result.add(entry);
                }
            }
        }
        return result;
    }

    private NoteJoueurResponse toResponse(NoteJoueur n) {
        return NoteJoueurResponse.builder()
                .id(n.getId())
                .joueurId(n.getJoueur().getId())
                .joueurNom(n.getJoueur().getNom())
                .joueurPrenom(n.getJoueur().getPrenom())
                .creneauId(n.getCreneau().getId())
                .creneauDescription(n.getCreneau().getJourSemaine() + " " + n.getCreneau().getHeureDebut() + "-" + n.getCreneau().getHeureFin())
                .entraineurId(n.getEntraineur().getId())
                .entraineurNom(n.getEntraineur().getPrenom() + " " + n.getEntraineur().getNom())
                .date(n.getDate())
                .physique(n.getPhysique())
                .technique(n.getTechnique())
                .explosivite(n.getExplosivite())
                .tactique(n.getTactique())
                .mental(n.getMental())
                .endurance(n.getEndurance())
                .noteGlobale(n.getNoteGlobale())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
