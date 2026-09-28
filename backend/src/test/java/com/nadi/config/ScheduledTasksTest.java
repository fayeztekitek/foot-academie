package com.nadi.config;

import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.service.DocumentService;
import com.nadi.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {

    @Mock
    private DocumentService documentService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private AcademieRepository academieRepository;

    private ScheduledTasks tasks() {
        return new ScheduledTasks(documentService, documentRepository, notificationService,
                utilisateurRepository, joueurRepository, paiementRepository, academieRepository);
    }

    private Utilisateur user(Long id) {
        return Utilisateur.builder()
                .id(id).email("p" + id + "@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
    }

    private Paiement pending(Parent parent, String amount, LocalDate due, StatutPaiement statut) {
        Joueur joueur = Joueur.builder().prenom("Amine").nom("Zouari")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(1L);
        return Paiement.builder()
                .joueur(joueur).parent(parent)
                .montant(new BigDecimal(amount)).devise("TND")
                .dateEcheance(due).statut(statut)
                .build();
    }

    private Parent parent(Long id, Utilisateur user) {
        Parent parent = Parent.builder().id(id).prenom("Salah").nom("Trabelsi")
                .email("p@nadi.tn").build();
        parent.setUtilisateur(user);
        return parent;
    }

    @Test
    void aggregatesPendingPerParentInOneNotification() {
        when(academieRepository.findAll()).thenReturn(
                List.of(Academie.builder().id(1L).slug("s").nom("N").build()));
        Utilisateur parentUser = user(42L);
        Parent parent = parent(10L, parentUser);
        when(paiementRepository.findPendingWithDetails()).thenReturn(List.of(
                pending(parent, "60", LocalDate.now().minusDays(5), StatutPaiement.EN_RETARD),
                pending(parent, "60", LocalDate.now().minusDays(35), StatutPaiement.EN_ATTENTE)));

        tasks().notifyPendingPayments();

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> details = ArgumentCaptor.forClass(String.class);
        verify(notificationService, times(1)).create(any(),
                eq(Notification.TypeNotification.PAIEMENT_RAPPEL),
                message.capture(), details.capture());
        assertTrue(message.getValue().contains("120"));
        assertTrue(message.getValue().contains("TND"));
        assertTrue(details.getValue().contains("Amine Zouari"));
    }

    @Test
    void skipsFutureDueDatesAndMissingAccounts() {
        when(academieRepository.findAll()).thenReturn(
                List.of(Academie.builder().id(1L).slug("s").nom("N").build()));
        Parent noAccount = parent(11L, null);
        when(paiementRepository.findPendingWithDetails()).thenReturn(List.of(
                pending(parent(10L, user(42L)), "60", LocalDate.now().plusDays(10), StatutPaiement.EN_ATTENTE),
                pending(noAccount, "60", LocalDate.now().minusDays(2), StatutPaiement.EN_RETARD)));

        tasks().notifyPendingPayments();

        verify(notificationService, never()).create(any(), any(), any(), any());
    }

    @Test
    void noPendingPaymentsSendsNothing() {
        when(academieRepository.findAll()).thenReturn(
                List.of(Academie.builder().id(1L).slug("s").nom("N").build()));
        when(paiementRepository.findPendingWithDetails()).thenReturn(List.of());

        tasks().notifyPendingPayments();

        verify(notificationService, never()).create(any(), any(), any(), any());
    }
}
