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
import java.util.List;
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

        List<Convocation> convocations = new ArrayList<>();

        if (request.getJoueurIds() != null) {
            for (Long joueurId : request.getJoueurIds()) {
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
        }

        if (request.getCategorieIds() != null) {
            for (Long catId : request.getCategorieIds()) {
                List<Joueur> joueurs = joueurRepository.findByCategorieId(catId);
                for (Joueur joueur : joueurs) {
                    if (!convocationRepository.existsByEvenementAndJoueur(evenement, joueur)) {
                        Convocation c = Convocation.builder()
                                .evenement(evenement)
                                .joueur(joueur)
                                .parent(joueur.getParent())
                                .build();
                        convocations.add(c);
                    }
                }
            }
        }

        convocations = convocationRepository.saveAll(convocations);

        for (Convocation conv : convocations) {
            if (conv.getParent() != null && conv.getParent().getUtilisateur() != null) {
                Notification notification = Notification.builder()
                        .utilisateur(conv.getParent().getUtilisateur())
                        .type(Notification.TypeNotification.COMPETITION)
                        .message("Convocation: " + evenement.getTitre())
                        .details("Votre enfant " + conv.getJoueur().getPrenom() + " " + conv.getJoueur().getNom()
                                + " est convocqué à l'événement \"" + evenement.getTitre() + "\""
                                + (evenement.getDateDebut() != null ? " le " + evenement.getDateDebut() : "")
                                + (evenement.getLieu() != null ? " à " + evenement.getLieu() : ""))
                        .build();
                notificationRepository.save(notification);
            }
        }

        return toResponse(evenement);
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

        return toResponse(evenementRepository.save(evenement));
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
