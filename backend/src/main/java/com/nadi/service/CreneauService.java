package com.nadi.service;

import com.nadi.dto.CreneauOccurrenceResponse;
import com.nadi.dto.CreneauRequest;
import com.nadi.dto.CreneauResponse;
import com.nadi.model.Categorie;
import com.nadi.model.Creneau;
import com.nadi.model.CreneauException;
import com.nadi.model.Entraineur;
import com.nadi.model.Joueur;
import com.nadi.model.JourSemaine;
import com.nadi.model.Notification;
import com.nadi.model.Utilisateur;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.CreneauExceptionRepository;
import com.nadi.repository.CreneauRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.AbsenceRepository;
import com.nadi.repository.JoueurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreneauService {

    private final CreneauRepository creneauRepository;
    private final CategorieRepository categorieRepository;
    private final EntraineurRepository entraineurRepository;
    private final AbsenceRepository absenceRepository;
    private final JoueurRepository joueurRepository;
    private final NotificationService notificationService;
    private final CreneauExceptionRepository creneauExceptionRepository;

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
        validateDateRange(request.getDateDebut(), request.getDateFin());

        validateNoOverlap(null, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));

        Creneau creneau = Creneau.builder()
                .jourSemaine(jour)
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .categorie(categorie)
                .terrain(request.getTerrain())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
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
        backfillLegacyCoach(creneau, entraineurs);

        Creneau saved = creneauRepository.save(creneau);
        notifyCreneauChange(saved, "Nouvel entraînement");
        return toResponse(saved);
    }

    /**
     * Legacy compatibility: databases created before multi-coach support have
     * a NOT NULL entraineur_id column, while current code only fills the
     * entraineurs set — every insert then failed in PostgreSQL.
     * (SchemaRepairMigration also drops the obsolete constraint.)
     */
    private static void backfillLegacyCoach(Creneau creneau, Set<Entraineur> entraineurs) {
        if (creneau.getEntraineur() == null && !entraineurs.isEmpty()) {
            creneau.setEntraineur(entraineurs.iterator().next());
        }
    }

    @Transactional
    public CreneauResponse update(Long id, CreneauRequest request) {
        Creneau creneau = creneauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + id));

        JourSemaine jour = JourSemaine.valueOf(request.getJourSemaine().toUpperCase());
        validateDateRange(request.getDateDebut(), request.getDateFin());

        validateNoOverlap(id, request.getTerrain(), jour, request.getHeureDebut(), request.getHeureFin());

        Categorie categorie = categorieRepository.findById(request.getCategorieId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + request.getCategorieId()));

        creneau.setJourSemaine(jour);
        creneau.setHeureDebut(request.getHeureDebut());
        creneau.setHeureFin(request.getHeureFin());
        creneau.setCategorie(categorie);
        creneau.setTerrain(request.getTerrain());
        creneau.setDateDebut(request.getDateDebut());
        creneau.setDateFin(request.getDateFin());

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
        backfillLegacyCoach(creneau, entraineurs);

        Creneau saved = creneauRepository.save(creneau);
        notifyCreneauChange(saved, "Horaire d'entraînement modifié");
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Creneau creneau = creneauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + id));
        notifyCreneauChange(creneau, "Entraînement supprimé");
        absenceRepository.deleteByCreneauId(id);
        creneauRepository.deleteById(id);
    }

    private static void validateDateRange(LocalDate debut, LocalDate fin) {
        if (debut != null && fin != null && fin.isBefore(debut)) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }
    }

    /**
     * Expands weekly templates into dated occurrences over [from, to],
     * clipped to each slot's own [dateDebut, dateFin] bounds and with
     * per-date exceptions applied (cancelled or modified with a reason).
     */
    @Transactional(readOnly = true)
    public List<CreneauOccurrenceResponse> getOccurrences(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new RuntimeException("Plage de dates invalide");
        }
        if (from.plusDays(366).isBefore(to)) {
            throw new RuntimeException("Plage maximale : 366 jours");
        }
        List<CreneauOccurrenceResponse> occurrences = new ArrayList<>();
        for (Creneau creneau : creneauRepository.findAll()) {
            DayOfWeek target = DayOfWeek.of(creneau.getJourSemaine().ordinal() + 1);
            Map<LocalDate, CreneauException> exceptions = new java.util.HashMap<>();
            for (CreneauException exception : creneauExceptionRepository.findByCreneauId(creneau.getId())) {
                exceptions.put(exception.getDate(), exception);
            }
            LocalDate start = from;
            if (creneau.getDateDebut() != null && creneau.getDateDebut().isAfter(start)) {
                start = creneau.getDateDebut();
            }
            LocalDate end = to;
            if (creneau.getDateFin() != null && creneau.getDateFin().isBefore(end)) {
                end = creneau.getDateFin();
            }
            LocalDate date = start;
            while (!date.isAfter(end)) {
                if (date.getDayOfWeek() == target) {
                    occurrences.add(toOccurrence(creneau, date, exceptions.get(date)));
                }
                date = date.plusDays(1);
            }
        }
        occurrences.sort((a, b) -> {
            int cmp = a.getDate().compareTo(b.getDate());
            if (cmp != 0) {
                return cmp;
            }
            return String.valueOf(a.getHeureDebut()).compareTo(String.valueOf(b.getHeureDebut()));
        });
        return occurrences;
    }

    private CreneauOccurrenceResponse toOccurrence(Creneau creneau, LocalDate date, CreneauException exception) {
        LocalTime heureDebut = creneau.getHeureDebut();
        LocalTime heureFin = creneau.getHeureFin();
        String terrain = creneau.getTerrain();
        String statut = "NORMALE";
        String motif = null;
        if (exception != null) {
            if (exception.getStatut() == CreneauException.StatutException.ANNULEE) {
                statut = "ANNULEE";
            } else {
                statut = "MODIFIEE";
                if (exception.getHeureDebut() != null) {
                    heureDebut = exception.getHeureDebut();
                }
                if (exception.getHeureFin() != null) {
                    heureFin = exception.getHeureFin();
                }
                if (exception.getTerrain() != null && !exception.getTerrain().isBlank()) {
                    terrain = exception.getTerrain();
                }
            }
            motif = exception.getMotif();
        }
        Set<String> coachNames = new java.util.TreeSet<>();
        if (creneau.getEntraineurs() != null) {
            for (Entraineur coach : creneau.getEntraineurs()) {
                coachNames.add(coach.getPrenom() + " " + coach.getNom());
            }
        }
        if (creneau.getEntraineur() != null) {
            coachNames.add(creneau.getEntraineur().getPrenom() + " " + creneau.getEntraineur().getNom());
        }
        return CreneauOccurrenceResponse.builder()
                .creneauId(creneau.getId())
                .date(date)
                .heureDebut(heureDebut)
                .heureFin(heureFin)
                .terrain(terrain)
                .categorieId(creneau.getCategorie() != null ? creneau.getCategorie().getId() : null)
                .categorieNom(creneau.getCategorie() != null ? creneau.getCategorie().getNom() : null)
                .coachNom(String.join(", ", coachNames))
                .statut(statut)
                .motif(motif)
                .build();
    }

    @Transactional
    public CreneauOccurrenceResponse saveException(Long creneauId, LocalDate date, String statut,
                                                  LocalTime heureDebut, LocalTime heureFin,
                                                  String terrain, String motif) {
        Creneau creneau = creneauRepository.findById(creneauId)
                .orElseThrow(() -> new RuntimeException("Créneau non trouvé: " + creneauId));
        if (date == null) {
            throw new RuntimeException("La date de l'exception est obligatoire");
        }
        if (motif == null || motif.isBlank()) {
            throw new RuntimeException("Un motif est obligatoire pour modifier une séance");
        }
        CreneauException.StatutException statutEnum;
        try {
            statutEnum = CreneauException.StatutException.valueOf(statut);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("Statut invalide (ANNULEE ou MODIFIEE)");
        }
        CreneauException exception = creneauExceptionRepository
                .findByCreneauIdAndDate(creneauId, date)
                .orElse(CreneauException.builder().creneau(creneau).date(date).build());
        exception.setStatut(statutEnum);
        exception.setHeureDebut(heureDebut);
        exception.setHeureFin(heureFin);
        exception.setTerrain(terrain);
        exception.setMotif(motif);
        creneauExceptionRepository.save(exception);
        return toOccurrence(creneau, date, exception);
    }

    @Transactional
    public void deleteException(Long creneauId, LocalDate date) {
        creneauExceptionRepository.deleteByCreneauIdAndDate(creneauId, date);
    }

    /**
     * Notifies the assigned coaches and the parents of the category's
     * players (one notification per account, children listed) with useful
     * details: day, hours, location and coach.
     */
    private void notifyCreneauChange(Creneau creneau, String action) {
        String categorie = creneau.getCategorie() != null ? creneau.getCategorie().getNom() : "?";
        String when = prettyDay(creneau.getJourSemaine())
                + " " + formatHour(creneau.getHeureDebut())
                + "-" + formatHour(creneau.getHeureFin());
        String where = creneau.getTerrain() != null ? creneau.getTerrain() : "terrain à confirmer";

        Set<Entraineur> coaches = new HashSet<>();
        if (creneau.getEntraineurs() != null) {
            coaches.addAll(creneau.getEntraineurs());
        }
        if (creneau.getEntraineur() != null) {
            coaches.add(creneau.getEntraineur());
        }
        String coachNames = coaches.stream()
                .map(e -> e.getPrenom() + " " + e.getNom())
                .sorted()
                .collect(Collectors.joining(", "));

        for (Entraineur coach : coaches) {
            if (coach.getUtilisateur() == null) {
                continue;
            }
            notificationService.create(coach.getUtilisateur(),
                    Notification.TypeNotification.CHANGEMENT_HORAIRE,
                    action + " " + categorie + " : " + when,
                    when + " — " + where + " (" + categorie + ")");
        }

        Map<Long, List<String>> childrenByUser = new LinkedHashMap<>();
        Map<Long, Utilisateur> usersById = new LinkedHashMap<>();
        if (creneau.getCategorie() != null) {
            for (Joueur joueur : joueurRepository.findByCategorieId(creneau.getCategorie().getId())) {
                if (joueur.getParent() == null || joueur.getParent().getUtilisateur() == null) {
                    continue;
                }
                Long userId = joueur.getParent().getUtilisateur().getId();
                usersById.putIfAbsent(userId, joueur.getParent().getUtilisateur());
                childrenByUser.computeIfAbsent(userId, k -> new ArrayList<>())
                        .add(joueur.getPrenom() + " " + joueur.getNom());
            }
        }
        for (Map.Entry<Long, List<String>> entry : childrenByUser.entrySet()) {
            List<String> children = entry.getValue().stream().sorted().toList();
            notificationService.create(usersById.get(entry.getKey()),
                    Notification.TypeNotification.CHANGEMENT_HORAIRE,
                    action + " (" + categorie + ")",
                    "Enfant(s) : " + String.join(", ", children)
                            + " — " + when + ", " + where
                            + (coachNames.isEmpty() ? "" : " (Coach : " + coachNames + ")"));
        }
    }

    private static String prettyDay(JourSemaine jour) {
        if (jour == null) {
            return "?";
        }
        String name = jour.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String formatHour(LocalTime time) {
        return time != null
                ? time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                : "?";
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
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
