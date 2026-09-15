package com.nadi.service;

import com.nadi.dto.PaiementRequest;
import com.nadi.dto.PaiementResponse;
import com.nadi.model.*;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.PaiementRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SecurityUtils securityUtils;

    private static final BigDecimal MONTANT_MENSUEL = new BigDecimal("60.000");
    private static final BigDecimal MONTANT_TRIMESTRIEL = new BigDecimal("180.000");
    private static final BigDecimal MONTANT_SEMESTRIEL = new BigDecimal("360.000");
    private static final BigDecimal MONTANT_ANNUEL = new BigDecimal("720.000");

    @Transactional(readOnly = true)
    public Page<PaiementResponse> getAll(Pageable pageable) {
        return paiementRepository.findAllWithDetails(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaiementResponse> getByStatut(String statut, Pageable pageable) {
        StatutPaiement s = StatutPaiement.valueOf(statut.toUpperCase());
        return paiementRepository.findByStatutWithDetails(s, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaiementResponse> getByJoueur(Long joueurId, Pageable pageable) {
        return paiementRepository.findByJoueurId(joueurId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaiementResponse> getByParent(Long parentId, Pageable pageable) {
        return paiementRepository.findByParentId(parentId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PaiementResponse> getOverdue() {
        return paiementRepository.findOverdue(LocalDate.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaiementResponse> getByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return paiementRepository.findByDateRange(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaiementResponse create(PaiementRequest request) {
        Joueur joueur = joueurRepository.findById(request.getJoueurId())
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + request.getJoueurId()));
        Parent parent = parentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + request.getParentId()));

        Utilisateur enregistrePar = null;
        if (securityUtils.getCurrentUserId() != null) {
            enregistrePar = utilisateurRepository.findById(securityUtils.getCurrentUserId()).orElse(null);
        }

        StatutPaiement statutInitial = StatutPaiement.EN_ATTENTE;
        if (request.getStatut() != null && !request.getStatut().isBlank()) {
            statutInitial = StatutPaiement.valueOf(request.getStatut().toUpperCase());
        }

        Paiement paiement = Paiement.builder()
                .joueur(joueur)
                .parent(parent)
                .montant(request.getMontant())
                .devise(request.getDevise() != null ? request.getDevise() : "TND")
                .dateEcheance(request.getDateEcheance())
                .statut(statutInitial)
                .moyenPaiement(request.getMoyenPaiement() != null ? MoyenPaiement.valueOf(request.getMoyenPaiement()) : null)
                .formule(request.getFormule() != null ? FormulePaiement.valueOf(request.getFormule()) : null)
                .dateEncaissement(request.getDateEncaissement())
                .numeroRecu(request.getNumeroRecu())
                .commentaire(request.getCommentaire())
                .notes(request.getNotes())
                .enregistrePar(enregistrePar)
                .build();
        return toResponse(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementResponse update(Long id, PaiementRequest request) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + id));

        if (request.getMontant() != null) paiement.setMontant(request.getMontant());
        if (request.getDevise() != null) paiement.setDevise(request.getDevise());
        if (request.getDateEcheance() != null) paiement.setDateEcheance(request.getDateEcheance());
        if (request.getMoyenPaiement() != null) {
            paiement.setMoyenPaiement(MoyenPaiement.valueOf(request.getMoyenPaiement()));
            paiement.setStatut(StatutPaiement.PAYE);
            paiement.setDatePaiement(LocalDate.now());
        }
        if (request.getFormule() != null) paiement.setFormule(FormulePaiement.valueOf(request.getFormule()));
        if (request.getDateEncaissement() != null) paiement.setDateEncaissement(request.getDateEncaissement());
        if (request.getNumeroRecu() != null) paiement.setNumeroRecu(request.getNumeroRecu());
        if (request.getCommentaire() != null) paiement.setCommentaire(request.getCommentaire());
        if (request.getNotes() != null) paiement.setNotes(request.getNotes());

        if (request.getMoyenPaiement() != null && securityUtils.getCurrentUserId() != null) {
            Utilisateur enregistrePar = utilisateurRepository.findById(securityUtils.getCurrentUserId()).orElse(null);
            paiement.setEnregistrePar(enregistrePar);
        }

        updateJoueurStatut(paiement.getJoueur().getId());
        return toResponse(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementResponse markAsPaid(Long id, String moyenPaiement, String numeroRecu, String commentaire, java.math.BigDecimal montant, LocalDate datePaiement) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + id));
        paiement.setStatut(StatutPaiement.PAYE);
        paiement.setDatePaiement(datePaiement != null ? datePaiement : LocalDate.now());
        paiement.setDateEncaissement(datePaiement != null ? datePaiement : LocalDate.now());
        if (montant != null) {
            paiement.setMontant(montant);
        }
        if (moyenPaiement != null) {
            paiement.setMoyenPaiement(MoyenPaiement.valueOf(moyenPaiement));
        } else {
            paiement.setMoyenPaiement(MoyenPaiement.ESPECES);
        }
        if (numeroRecu != null) paiement.setNumeroRecu(numeroRecu);
        if (commentaire != null) paiement.setCommentaire(commentaire);

        if (securityUtils.getCurrentUserId() != null) {
            Utilisateur enregistrePar = utilisateurRepository.findById(securityUtils.getCurrentUserId()).orElse(null);
            paiement.setEnregistrePar(enregistrePar);
        }

        updateJoueurStatut(paiement.getJoueur().getId());
        return toResponse(paiementRepository.save(paiement));
    }

    @Transactional
    public void generateSchedule(Long joueurId, Long parentId, FormulePaiement formule, LocalDate startDate) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + parentId));

        BigDecimal montant;
        int numberOfPayments;
        int monthsStep;

        switch (formule) {
            case MENSUEL -> {
                montant = MONTANT_MENSUEL;
                numberOfPayments = 12;
                monthsStep = 1;
            }
            case TRIMESTRIEL -> {
                montant = MONTANT_TRIMESTRIEL;
                numberOfPayments = 4;
                monthsStep = 3;
            }
            case SEMESTRIEL -> {
                montant = MONTANT_SEMESTRIEL;
                numberOfPayments = 2;
                monthsStep = 6;
            }
            case ANNUEL -> {
                montant = MONTANT_ANNUEL;
                numberOfPayments = 1;
                monthsStep = 12;
            }
            default -> throw new RuntimeException("Formule inconnue: " + formule);
        }

        LocalDate echeance = startDate;
        for (int i = 0; i < numberOfPayments; i++) {
            Paiement paiement = Paiement.builder()
                    .joueur(joueur)
                    .parent(parent)
                    .montant(montant)
                    .devise("TND")
                    .dateEcheance(echeance)
                    .statut(StatutPaiement.EN_ATTENTE)
                    .formule(formule)
                    .build();
            paiementRepository.save(paiement);
            echeance = echeance.plusMonths(monthsStep);
        }
    }

    @Transactional
    public void recalculateFuturePayments(Long joueurId, FormulePaiement newFormule) {
        List<Paiement> existing = paiementRepository.findByJoueurIdAndStatut(joueurId, StatutPaiement.EN_ATTENTE);
        existing.addAll(paiementRepository.findByJoueurIdAndStatut(joueurId, StatutPaiement.EN_RETARD));

        LocalDate cutoff = LocalDate.now();
        List<Paiement> futureUnpaid = existing.stream()
                .filter(p -> !p.getDateEcheance().isBefore(cutoff))
                .collect(Collectors.toList());

        paiementRepository.deleteAll(futureUnpaid);

        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));
        Parent parent = joueur.getParent();
        if (parent == null) return;

        LocalDate nextStart = cutoff.withDayOfMonth(1);
        if (nextStart.isBefore(cutoff)) {
            nextStart = nextStart.plusMonths(1);
        }

        BigDecimal montant;
        int monthsStep;
        switch (newFormule) {
            case MENSUEL -> { montant = MONTANT_MENSUEL; monthsStep = 1; }
            case TRIMESTRIEL -> { montant = MONTANT_TRIMESTRIEL; monthsStep = 3; }
            case SEMESTRIEL -> { montant = MONTANT_SEMESTRIEL; monthsStep = 6; }
            case ANNUEL -> { montant = MONTANT_ANNUEL; monthsStep = 12; }
            default -> throw new RuntimeException("Formule inconnue: " + newFormule);
        }

        LocalDate echeance = nextStart;
        for (int i = 0; i < 12 / monthsStep; i++) {
            Paiement paiement = Paiement.builder()
                    .joueur(joueur)
                    .parent(parent)
                    .montant(montant)
                    .devise("TND")
                    .dateEcheance(echeance)
                    .statut(StatutPaiement.EN_ATTENTE)
                    .formule(newFormule)
                    .build();
            paiementRepository.save(paiement);
            echeance = echeance.plusMonths(monthsStep);
        }
    }

    @Transactional
    public void delete(Long id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé: " + id));
        Long joueurId = paiement.getJoueur().getId();
        paiementRepository.deleteById(id);
        updateJoueurStatut(joueurId);
    }

    @Transactional
    public void checkOverdue() {
        List<Paiement> overdue = paiementRepository.findOverdue(LocalDate.now());
        for (Paiement p : overdue) {
            p.setStatut(StatutPaiement.EN_RETARD);
            paiementRepository.save(p);
        }
    }

    private void updateJoueurStatut(Long joueurId) {
        List<Paiement> unpaid = paiementRepository.findByJoueurIdAndStatut(joueurId, StatutPaiement.EN_ATTENTE);
        unpaid.addAll(paiementRepository.findByJoueurIdAndStatut(joueurId, StatutPaiement.EN_RETARD));

        Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
        if (joueur != null) {
            if (unpaid.isEmpty()) {
                joueur.setStatutPaiement(Joueur.StatutPaiement.A_JOUR);
            } else {
                boolean hasOverdue = unpaid.stream().anyMatch(p ->
                        p.getDateEcheance().isBefore(LocalDate.now()));
                joueur.setStatutPaiement(hasOverdue ? Joueur.StatutPaiement.EN_RETARD : Joueur.StatutPaiement.IMPAYE);
            }
            joueurRepository.save(joueur);
        }
    }

    private PaiementResponse toResponse(Paiement p) {
        return PaiementResponse.builder()
                .id(p.getId())
                .joueurId(p.getJoueur().getId())
                .joueurNom(p.getJoueur().getNom())
                .joueurPrenom(p.getJoueur().getPrenom())
                .parentId(p.getParent().getId())
                .parentNom(p.getParent().getNom())
                .parentPrenom(p.getParent().getPrenom())
                .montant(p.getMontant())
                .devise(p.getDevise())
                .dateEcheance(p.getDateEcheance())
                .datePaiement(p.getDatePaiement())
                .dateEncaissement(p.getDateEncaissement())
                .statut(p.getStatut().name())
                .moyenPaiement(p.getMoyenPaiement() != null ? p.getMoyenPaiement().name() : null)
                .formule(p.getFormule() != null ? p.getFormule().name() : null)
                .frequence(p.getFormule() != null ? p.getFormule().name() : null)
                .reference(p.getReference())
                .numeroRecu(p.getNumeroRecu())
                .commentaire(p.getCommentaire())
                .notes(p.getNotes())
                .enregistreParId(p.getEnregistrePar() != null ? p.getEnregistrePar().getId() : null)
                .enregistreParEmail(p.getEnregistrePar() != null ? p.getEnregistrePar().getEmail() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
