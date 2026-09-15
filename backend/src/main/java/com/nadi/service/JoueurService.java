package com.nadi.service;

import com.nadi.dto.JoueurRequest;
import com.nadi.dto.JoueurResponse;
import com.nadi.model.Categorie;
import com.nadi.model.FormulePaiement;
import com.nadi.model.Joueur;
import com.nadi.model.Paiement;
import com.nadi.model.Parent;
import com.nadi.model.StatutPaiement;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.PaiementRepository;
import com.nadi.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final CategorieRepository categorieRepository;
    private final ParentRepository parentRepository;
    private final PaiementService paiementService;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public Page<JoueurResponse> getAll(Pageable pageable) {
        return joueurRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JoueurResponse> search(String query, Pageable pageable) {
        return joueurRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(query, query, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JoueurResponse> getByCategorie(Long categorieId, Pageable pageable) {
        return joueurRepository.findByCategorieId(categorieId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JoueurResponse> getByParent(Long parentId, Pageable pageable) {
        return joueurRepository.findByParentId(parentId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JoueurResponse getById(Long id) {
        Joueur joueur = joueurRepository.findByIdWithDetails(id);
        if (joueur == null) {
            throw new RuntimeException("Joueur non trouvé: " + id);
        }
        return toResponse(joueur);
    }

    @Transactional
    public JoueurResponse create(JoueurRequest request) {
        Joueur joueur = Joueur.builder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .dateNaissance(request.getDateNaissance())
                .dateEntree(request.getDateEntree() != null ? request.getDateEntree() : LocalDate.now())
                .photoUrl(request.getPhotoUrl())
                .certificatMedical(Boolean.TRUE.equals(request.getCertificatMedical()))
                .autorisationParentale(Boolean.TRUE.equals(request.getAutorisationParentale()))
                .build();

        if (request.getFrequence() != null && !request.getFrequence().isBlank()) {
            joueur.setFrequence(FormulePaiement.valueOf(request.getFrequence()));
        }

        if (request.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));
            joueur.setCategorie(categorie);
        }

        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + request.getParentId()));
            joueur.setParent(parent);
        }

        return toResponse(joueurRepository.save(joueur));
    }

    @Transactional
    public JoueurResponse update(Long id, JoueurRequest request) {
        Joueur joueur = joueurRepository.findByIdWithDetails(id);
        if (joueur == null) {
            throw new RuntimeException("Joueur non trouvé: " + id);
        }

        joueur.setPrenom(request.getPrenom());
        joueur.setNom(request.getNom());
        joueur.setDateNaissance(request.getDateNaissance());
        if (request.getDateEntree() != null) {
            joueur.setDateEntree(request.getDateEntree());
        }
        joueur.setPhotoUrl(request.getPhotoUrl());

        if (request.getCertificatMedical() != null) {
            joueur.setCertificatMedical(request.getCertificatMedical());
        }
        if (request.getAutorisationParentale() != null) {
            joueur.setAutorisationParentale(request.getAutorisationParentale());
        }

        if (request.getFrequence() != null && !request.getFrequence().isBlank()) {
            joueur.setFrequence(FormulePaiement.valueOf(request.getFrequence()));
        }

        if (request.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(request.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));
            joueur.setCategorie(categorie);
        }

        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + request.getParentId()));
            joueur.setParent(parent);
        }

        return toResponse(joueurRepository.save(joueur));
    }

    @Transactional
    public JoueurResponse changeFrequence(Long id, String frequence) {
        Joueur joueur = joueurRepository.findByIdWithDetails(id);
        if (joueur == null) {
            throw new RuntimeException("Joueur non trouvé: " + id);
        }

        FormulePaiement newFormule = FormulePaiement.valueOf(frequence);
        joueur.setFrequence(newFormule);
        joueurRepository.save(joueur);

        paiementService.recalculateFuturePayments(id, newFormule);

        return toResponse(joueur);
    }

    @Transactional
    public void delete(Long id) {
        if (!joueurRepository.existsById(id)) {
            throw new RuntimeException("Joueur non trouvé: " + id);
        }
        joueurRepository.deleteById(id);
    }

    private JoueurResponse toResponse(Joueur j) {
        LocalDate dateEntree = j.getDateEntree();
        YearMonth debutSaison = getDebutSaison(YearMonth.now());
        YearMonth finSaison = debutSaison.plusMonths(11);

        int moisAVerser = 0;
        int moisPayes = 0;
        int moisImpayes = 0;

        if (dateEntree != null) {
            YearMonth entree = YearMonth.from(dateEntree);
            if (entree.isAfter(finSaison)) {
                entree = debutSaison;
            }
            long totalMois = YearMonth.from(finSaison).lengthOfMonth() == YearMonth.from(entree).lengthOfMonth()
                    ? java.time.temporal.ChronoUnit.MONTHS.between(entree, finSaison) + 1
                    : java.time.temporal.ChronoUnit.MONTHS.between(entree, finSaison) + 1;
            moisAVerser = (int) totalMois;

            List<Paiement> paiements = paiementRepository.findByJoueurId(j.getId());
            long payes = paiements.stream()
                    .filter(p -> p.getStatut() == StatutPaiement.PAYE)
                    .count();
            moisPayes = (int) payes;
            moisImpayes = Math.max(0, moisAVerser - moisPayes);
        }

        return JoueurResponse.builder()
                .id(j.getId())
                .prenom(j.getPrenom())
                .nom(j.getNom())
                .dateNaissance(j.getDateNaissance())
                .dateEntree(j.getDateEntree())
                .categorieId(j.getCategorie() != null ? j.getCategorie().getId() : null)
                .categorieNom(j.getCategorie() != null ? j.getCategorie().getNom() : null)
                .parentId(j.getParent() != null ? j.getParent().getId() : null)
                .parentNom(j.getParent() != null ? j.getParent().getNom() : null)
                .parentPrenom(j.getParent() != null ? j.getParent().getPrenom() : null)
                .statutPaiement(j.getStatutPaiement().name())
                .frequence(j.getFrequence() != null ? j.getFrequence().name() : null)
                .photoUrl(j.getPhotoUrl())
                .certificatMedical(j.getCertificatMedical())
                .autorisationParentale(j.getAutorisationParentale())
                .moisAVerser(moisAVerser)
                .moisPayes(moisPayes)
                .moisImpayes(moisImpayes)
                .createdAt(j.getCreatedAt())
                .build();
    }

    private YearMonth getDebutSaison(YearMonth now) {
        if (now.getMonthValue() >= 9) {
            return YearMonth.of(now.getYear(), 9);
        } else {
            return YearMonth.of(now.getYear() - 1, 9);
        }
    }
}
