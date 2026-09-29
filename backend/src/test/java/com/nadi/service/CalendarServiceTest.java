package com.nadi.service;

import com.nadi.model.*;
import com.nadi.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ConvocationRepository convocationRepository;
    @Mock
    private CreneauRepository creneauRepository;
    @Mock
    private EvenementRepository evenementRepository;
    @Mock
    private EntraineurRepository entraineurRepository;

    private CalendarService service() {
        return new CalendarService(utilisateurRepository, convocationRepository,
                creneauRepository, evenementRepository, entraineurRepository);
    }

    private Utilisateur parentUser() {
        return Utilisateur.builder()
                .id(42L).email("p@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
    }

    @Test
    void unknownTokenIsRejected() {
        when(utilisateurRepository.findByCalendarToken("nope")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service().buildFeed("nope"));
    }

    @Test
    void blankTokenIsRejected() {
        assertThrows(RuntimeException.class, () -> service().buildFeed("  "));
    }

    @Test
    void parentFeedContainsConvocationEvent() {
        Utilisateur user = parentUser();
        when(utilisateurRepository.findByCalendarToken("tok")).thenReturn(Optional.of(user));
        Joueur joueur = Joueur.builder().prenom("Amine").nom("Zouari")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(1L);
        Evenement event = Evenement.builder().titre("Tournoi")
                .typeEvenement(Evenement.TypeEvenement.TOURNOI)
                .dateDebut(LocalDate.now().plusDays(3)).lieu("Tunis").build();
        event.setId(9L);
        Convocation convocation = Convocation.builder().id(5L)
                .evenement(event).joueur(joueur)
                .statut(Convocation.StatutConvocation.INVITE).build();
        when(convocationRepository.findByParentUserId(42L)).thenReturn(List.of(convocation));

        String feed = service().buildFeed("tok");

        assertTrue(feed.contains("BEGIN:VCALENDAR"));
        assertTrue(feed.contains("BEGIN:VEVENT"));
        assertTrue(feed.contains("Convocation : Tournoi"));
        assertTrue(feed.contains("Amine Zouari"));
        assertTrue(feed.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    void regenerateSetsOpaqueToken() {
        Utilisateur user = parentUser();
        when(utilisateurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String token = service().regenerateToken(user);

        assertEquals(64, token.length());
        assertTrue(token.matches("[0-9a-f]+"));
        assertEquals(token, user.getCalendarToken());
    }
}
