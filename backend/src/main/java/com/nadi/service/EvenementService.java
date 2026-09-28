package com.nadi.service;

import com.nadi.dto.ConvocationResponse;
import com.nadi.dto.EvenementRequest;
import com.nadi.dto.EvenementResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final ConvocationRepository convocationRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<EvenementResponse> getAll() {
        return evenementRepository.findAllOrdered().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EvenementResponse> getByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return evenementRepository.findByDateDebutBetween(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EvenementResponse create(EvenementRequest request) {
        Utilisateur currentUser = securityUtils.getCurrentUserOrThrow();

        Evenement evenement = Evenement.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .typeEvenement(Evenement.TypeEvenement.valueOf(request.getTypeEvenement()))
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin() != null ? request.getDateFin() : request.getDateDebut())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .lieu(request.getLieu())
                .terrain(request.getTerrain())
                .creePar(currentUser)
                .build();

        evenement = evenementRepository.save(evenement);

        // Dedupe by joueur: a player selected individually AND through their
        // category (or twice) must yield a single convocation. The DB
        // exists-check alone cannot catch in-memory duplicates.
        Set<Long> joueurIds = new java.util.LinkedHashSet<>();
        if (request.getJoueurIds() != null) {
            for (Long joueurId : request.getJoueurIds()) {
                if (joueurId != null) {
                    joueurIds.add(joueurId);
                }
            }
        }
        if (request.getCategorieIds() != null) {
            for (Long catId : request.getCategorieIds()) {
                if (catId == null) {
                    continue;
                }
                for (Joueur joueur : joueurRepository.findByCategorieId(catId)) {
                    joueurIds.add(joueur.getId());
                }
            }
        }

        List<Convocation> convocations = new ArrayList<>();
        for (Long joueurId : joueurIds) {
            Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
            if (joueur != null && !convocationRepository.existsByEvenementAndJoueur(evenement, joueur)) {
                Convocation c = Convocation.builder()
                        .evenement(evenement)
                        .joueur(joueur)
                        .parent(joueur.getParent())
                        .build();
                convocations.add(c);
            }
        }

        convocations = convocationRepository.saveAll(convocations);

        Set<Long> alreadyNotified = new HashSet<>();
        for (Convocation conv : convocations) {
            if (conv.getParent() != null && conv.getParent().getUtilisateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(conv.getParent().getUtilisateur())
                        .type(Notification.TypeNotification.COMPETITION)
                        .message("Convocation: " + evenement.getTitre())
                        .details("Votre enfant " + conv.getJoueur().getPrenom() + " " + conv.getJoueur().getNom()
                                + " est convocqué à l'événement \"" + evenement.getTitre() + "\""
                                + describeWhenWhere(evenement))
                        .build();
                notificationRepository.save(notification);
                alreadyNotified.add(conv.getParent().getUtilisateur().getId());
            }
        }

        notifyAllUsersExcept(evenement, currentUser, alreadyNotified);

        return toResponse(evenement);
    }

    /**
     * Fan-out: every active account of the academy (coaches, parents linked
     * to an account, fellow admins) is notified of a new event — except its
     * creator and users already notified through their child's convocation.
     */
    private void notifyAllUsersExcept(Evenement evenement, Utilisateur creator, Set<Long> alreadyNotified) {
        String details = "Nouvel événement \""
                + evenement.getTitre() + "\""
                + (evenement.getDateDebut() != null ? " le " + evenement.getDateDebut() : "")
                + (evenement.getLieu() != null ? " à " + evenement.getLieu() : "");
        for (Utilisateur user : utilisateurRepository.findAll()) {
            if (user.getId() == null || Objects.equals(user.getId(), creator.getId())) {
                continue;
            }
            if (Boolean.FALSE.equals(user.getActif())) {
                continue;
            }
            if (alreadyNotified.contains(user.getId())) {
                continue;
            }
            notificationService.create(user, Notification.TypeNotification.COMPETITION,
                    "Nouvel événement : " + evenement.getTitre(), details);
        }
    }

    @Transactional
    public EvenementResponse update(Long id, EvenementRequest request) {
        Evenement evenement = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé: " + id));

        if (request.getTitre() != null) evenement.setTitre(request.getTitre());
        if (request.getDescription() != null) evenement.setDescription(request.getDescription());
        if (request.getTypeEvenement() != null) evenement.setTypeEvenement(Evenement.TypeEvenement.valueOf(request.getTypeEvenement()));
        if (request.getDateDebut() != null) evenement.setDateDebut(request.getDateDebut());
        if (request.getDateFin() != null) evenement.setDateFin(request.getDateFin());
        if (request.getHeureDebut() != null) evenement.setHeureDebut(request.getHeureDebut());
        if (request.getHeureFin() != null) evenement.setHeureFin(request.getHeureFin());
        if (request.getLieu() != null) evenement.setLieu(request.getLieu());
        if (request.getTerrain() != null) evenement.setTerrain(request.getTerrain());

        evenementRepository.save(evenement);

        if (request.getJoueurIds() != null || request.getCategorieIds() != null) {
            syncConvocations(evenement, request.getJoueurIds(), request.getCategorieIds());
        }

        return toResponse(evenement);
    }

    /**
     * Reconciles convocations with the desired roster: adds missing players
     * (notifying their parents) and removes withdrawn ones. A null list means
     * "leave unchanged", an empty list means "remove all from that source".
     */
    private void syncConvocations(Evenement evenement, List<Long> joueurIds, List<Long> categorieIds) {
        Set<Long> desired = new java.util.LinkedHashSet<>();
        if (joueurIds != null) {
            for (Long joueurId : joueurIds) {
                if (joueurId != null) {
                    desired.add(joueurId);
                }
            }
        }
        if (categorieIds != null) {
            for (Long catId : categorieIds) {
                if (catId == null) {
                    continue;
                }
                for (Joueur joueur : joueurRepository.findByCategorieId(catId)) {
                    desired.add(joueur.getId());
                }
            }
        }

        List<Convocation> existing = convocationRepository.findByEvenementId(evenement.getId());
        Set<Long> existingJoueurIds = new java.util.HashSet<>();
        List<Convocation> toRemove = new ArrayList<>();
        for (Convocation convocation : existing) {
            if (convocation.getJoueur() != null && desired.contains(convocation.getJoueur().getId())) {
                existingJoueurIds.add(convocation.getJoueur().getId());
            } else {
                toRemove.add(convocation);
            }
        }
        convocationRepository.deleteAll(toRemove);

        for (Long joueurId : desired) {
            if (existingJoueurIds.contains(joueurId)) {
                continue;
            }
            Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
            if (joueur == null) {
                continue;
            }
            Convocation convocation = convocationRepository.save(Convocation.builder()
                    .evenement(evenement)
                    .joueur(joueur)
                    .parent(joueur.getParent())
                    .build());
            notifyParent(convocation, evenement);
        }
    }

    private void notifyParent(Convocation convocation, Evenement evenement) {
        if (convocation.getParent() == null || convocation.getParent().getUtilisateur() == null) {
            return;
        }
        notificationService.create(convocation.getParent().getUtilisateur(),
                Notification.TypeNotification.COMPETITION,
                "Convocation: " + evenement.getTitre(),
                "Votre enfant " + convocation.getJoueur().getPrenom() + " " + convocation.getJoueur().getNom()
                        + " est convocqué à l'événement \"" + evenement.getTitre() + "\""
                        + describeWhenWhere(evenement));
    }

    private static String describeWhenWhere(Evenement evenement) {
        StringBuilder details = new StringBuilder();
        if (evenement.getDateDebut() != null) {
            details.append(" le ").append(evenement.getDateDebut());
        }
        if (evenement.getHeureDebut() != null) {
            details.append(" à ").append(evenement.getHeureDebut());
            if (evenement.getHeureFin() != null) {
                details.append("-").append(evenement.getHeureFin());
            }
        }
        if (evenement.getLieu() != null) {
            details.append(", ").append(evenement.getLieu());
        }
        return details.toString();
    }

    @Transactional
    public void delete(Long id) {
        evenementRepository.deleteById(id);
    }

    @Transactional
    public List<ConvocationResponse> getConvocations(Long evenementId) {
        return convocationRepository.findByEvenementId(evenementId).stream()
                .map(this::toConvocationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ConvocationResponse> getMyConvocations() {
        Long userId = securityUtils.getCurrentUserId();
        return convocationRepository.findByParentUserId(userId).stream()
                .map(this::toConvocationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConvocationResponse respondToConvocation(Long convocationId, boolean accept) {
        Convocation convocation = convocationRepository.findById(convocationId)
                .orElseThrow(() -> new RuntimeException("Convocation non trouvée: " + convocationId));

        // A parent may only respond to their own family's convocations.
        Long userId = securityUtils.getCurrentUserId();
        boolean ownFamily = (convocation.getParent() != null
                && convocation.getParent().getUtilisateur() != null
                && convocation.getParent().getUtilisateur().getId().equals(userId))
                || (convocation.getJoueur() != null
                && convocation.getJoueur().getParent() != null
                && convocation.getJoueur().getParent().getUtilisateur() != null
                && convocation.getJoueur().getParent().getUtilisateur().getId().equals(userId));
        if (!ownFamily) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Accès interdit à cette convocation");
        }

        convocation.setStatut(accept ? Convocation.StatutConvocation.CONFIRME : Convocation.StatutConvocation.REFUSE);
        convocation.setDateReponse(LocalDateTime.now());

        return toConvocationResponse(convocationRepository.save(convocation));
    }

    private EvenementResponse toResponse(Evenement e) {
        List<Convocation> convocations = convocationRepository.findByEvenement(e);
        long nbConfirmes = convocations.stream()
                .filter(c -> c.getStatut() == Convocation.StatutConvocation.CONFIRME)
                .count();

        return EvenementResponse.builder()
                .id(e.getId())
                .titre(e.getTitre())
                .description(e.getDescription())
                .typeEvenement(e.getTypeEvenement().name())
                .dateDebut(e.getDateDebut())
                .dateFin(e.getDateFin())
                .heureDebut(e.getHeureDebut())
                .heureFin(e.getHeureFin())
                .lieu(e.getLieu())
                .terrain(e.getTerrain())
                .creeParId(e.getCreePar() != null ? e.getCreePar().getId() : null)
                .creeParNom(e.getCreePar() != null ? e.getCreePar().getEmail() : null)
                .nbConvocations(convocations.size())
                .nbConfirmes((int) nbConfirmes)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ConvocationResponse toConvocationResponse(Convocation c) {
        return ConvocationResponse.builder()
                .id(c.getId())
                .evenementId(c.getEvenement().getId())
                .evenementTitre(c.getEvenement().getTitre())
                .evenementType(c.getEvenement().getTypeEvenement().name())
                .evenementDateDebut(c.getEvenement().getDateDebut())
                .evenementLieu(c.getEvenement().getLieu())
                .joueurId(c.getJoueur().getId())
                .joueurPrenom(c.getJoueur().getPrenom())
                .joueurNom(c.getJoueur().getNom())
                .categorieNom(c.getJoueur().getCategorie() != null ? c.getJoueur().getCategorie().getNom() : null)
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .parentPrenom(c.getParent() != null ? c.getParent().getPrenom() : null)
                .parentNom(c.getParent() != null ? c.getParent().getNom() : null)
                .parentEmail(c.getParent() != null && c.getParent().getUtilisateur() != null ? c.getParent().getUtilisateur().getEmail() : null)
                .statut(c.getStatut().name())
                .dateReponse(c.getDateReponse())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
