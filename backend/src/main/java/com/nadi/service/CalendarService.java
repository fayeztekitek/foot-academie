package com.nadi.service;

import com.nadi.model.*;
import com.nadi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Read-only iCalendar subscription feed, authenticated by an opaque
 * per-user token (no login required to fetch — the token IS the secret).
 * Content is scoped by the token owner's role: parents see their
 * convocations, coaches their training slots (12 upcoming weekly
 * occurrences) plus academy events, staff see upcoming events.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UtilisateurRepository utilisateurRepository;
    private final ConvocationRepository convocationRepository;
    private final CreneauRepository creneauRepository;
    private final EvenementRepository evenementRepository;
    private final EntraineurRepository entraineurRepository;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public String regenerateToken(Utilisateur user) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        user.setCalendarToken(token);
        utilisateurRepository.save(user);
        return token;
    }

    @Transactional(readOnly = true)
    public String buildFeed(String token) {
        if (token == null || token.isBlank()) {
            throw new RuntimeException("Lien de calendrier invalide");
        }
        Utilisateur user = utilisateurRepository.findByCalendarToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de calendrier invalide"));
        if (Boolean.FALSE.equals(user.getActif())) {
            throw new RuntimeException("Lien de calendrier invalide");
        }

        List<String> events = new ArrayList<>();
        if (user.getRole() == Role.PARENT) {
            for (Convocation convocation : convocationRepository.findByParentUserId(user.getId())) {
                events.add(convocationEvent(convocation));
            }
        } else if (user.getRole() == Role.COACH) {
            events.addAll(coachSlotEvents(user));
            events.addAll(upcomingAcademyEvents());
        } else {
            events.addAll(upcomingAcademyEvents());
        }

        StringBuilder feed = new StringBuilder();
        feed.append("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Nadi Academie//Calendrier//FR\r\n");
        feed.append("X-WR-CALNAME:Nadi Académie\r\n");
        for (String event : events) {
            feed.append(event);
        }
        feed.append("END:VCALENDAR\r\n");
        return feed.toString();
    }

    private String convocationEvent(Convocation convocation) {
        Evenement event = convocation.getEvenement();
        Joueur joueur = convocation.getJoueur();
        StringBuilder details = new StringBuilder();
        if (joueur != null) {
            details.append("Enfant : ").append(joueur.getPrenom()).append(" ").append(joueur.getNom()).append("\\n");
        }
        if (joueur != null && joueur.getCategorie() != null) {
            details.append("Catégorie : ").append(joueur.getCategorie().getNom()).append("\\n");
        }
        details.append("Statut : ").append(convocation.getStatut() != null ? convocation.getStatut().name() : "?");
        return vevent(
                "convocation-" + convocation.getId() + "@nadi",
                "Convocation : " + event.getTitre(),
                event.getDateDebut(), event.getHeureDebut(), event.getHeureFin(),
                event.getLieu(), details.toString());
    }

    private List<String> coachSlotEvents(Utilisateur user) {
        List<String> events = new ArrayList<>();
        List<Entraineur> coaches = new ArrayList<>();
        entraineurRepository.findByUtilisateurId(user.getId()).ifPresent(coaches::add);
        for (Entraineur coach : coaches) {
            List<Creneau> slots = new ArrayList<>(creneauRepository.findByEntraineurId(coach.getId()));
            for (Creneau extra : creneauRepository.findByEntraineursId(coach.getId())) {
                if (slots.stream().noneMatch(s -> s.getId().equals(extra.getId()))) {
                    slots.add(extra);
                }
            }
            // Next 12 weekly occurrences per slot.
            for (Creneau slot : slots) {
                LocalDate day = nextWeekday(slot.getJourSemaine());
                for (int week = 0; week < 12 && events.size() < 200; week++) {
                    LocalDate date = day.plusWeeks(week);
                    events.add(vevent(
                            "creneau-" + slot.getId() + "-" + date + "@nadi",
                            "Entraînement " + (slot.getCategorie() != null ? slot.getCategorie().getNom() : ""),
                            date, toStringOrNull(slot.getHeureDebut()), toStringOrNull(slot.getHeureFin()),
                            slot.getTerrain(), null));
                }
            }
        }
        return events;
    }

    private List<String> upcomingAcademyEvents() {
        List<String> events = new ArrayList<>();
        LocalDate from = LocalDate.now().minusDays(7);
        for (Evenement event : evenementRepository.findAll()) {
            if (event.getDateDebut() == null || event.getDateDebut().isBefore(from)) {
                continue;
            }
            if (events.size() >= 100) {
                break;
            }
            events.add(vevent(
                    "evenement-" + event.getId() + "@nadi",
                    event.getTitre(),
                    event.getDateDebut(), event.getHeureDebut(), event.getHeureFin(),
                    event.getLieu(), event.getDescription()));
        }
        events.sort(Comparator.naturalOrder());
        return events;
    }

    private static LocalDate nextWeekday(JourSemaine jour) {
        DayOfWeek target = DayOfWeek.valueOf(jour.name());
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != target) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static String toStringOrNull(LocalTime time) {
        return time != null ? time.toString() : null;
    }

    private static String vevent(String uid, String summary, LocalDate date,
                                 String startHour, String endHour, String location, String description) {
        StringBuilder event = new StringBuilder();
        event.append("BEGIN:VEVENT\r\nUID:").append(uid).append("\r\n");
        String start = toTimeStamp(date, startHour);
        if (start != null) {
            event.append("DTSTART:").append(start).append("\r\n");
            String end = toTimeStamp(date, endHour);
            if (end != null) {
                event.append("DTEND:").append(end).append("\r\n");
            }
        } else if (date != null) {
            event.append("DTSTART;VALUE=DATE:").append(date.format(DATE)).append("\r\n");
        }
        event.append("SUMMARY:").append(escape(summary)).append("\r\n");
        if (location != null && !location.isBlank()) {
            event.append("LOCATION:").append(escape(location)).append("\r\n");
        }
        if (description != null && !description.isBlank()) {
            event.append("DESCRIPTION:").append(escape(description)).append("\r\n");
        }
        event.append("END:VEVENT\r\n");
        return event.toString();
    }

    private static String toTimeStamp(LocalDate date, String hour) {
        if (date == null || hour == null || hour.isBlank()) {
            return null;
        }
        String digits = hour.replace(":", "");
        if (digits.length() == 4) {
            digits += "00";
        }
        if (digits.length() != 6 || !digits.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return date.format(DATE) + "T" + digits;
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,")
                .replace("\r\n", "\\n").replace("\n", "\\n");
    }
}
