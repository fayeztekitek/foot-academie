package com.nadi.service;

import com.nadi.model.*;
import com.nadi.repository.GenerationMensuelleRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.PaiementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyPaymentServiceTest {

    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private PaiementRepository paiementRepository;
    @Mock
    private GenerationMensuelleRepository generationRepository;

    private MonthlyPaymentService service() {
        return new MonthlyPaymentService(joueurRepository, paiementRepository, generationRepository);
    }

    private Parent parent() {
        return Parent.builder().id(10L).prenom("Salah").nom("Trabelsi").email("s@t.tn").build();
    }

    private Joueur eligiblePlayer() {
        Joueur joueur = Joueur.builder().prenom("Mohamed").nom("T")
                .dateNaissance(LocalDate.of(2016, 3, 1)).build();
        joueur.setId(1L);
        joueur.setParent(parent());
        joueur.setDateEntree(LocalDate.now().minusMonths(2));
        joueur.setFrequence(FormulePaiement.MENSUEL);
        return joueur;
    }

    private final YearMonth month = YearMonth.now();

    @Test
    void skipsAlreadyGeneratedMonth() {
        when(generationRepository.existsByTenantIdAndMois(1L, month.toString())).thenReturn(true);

        assertEquals(0, service().ensureMonthlyPayments(1L, month));

        verify(joueurRepository, never()).findAll();
        verify(paiementRepository, never()).save(any());
    }

    @Test
    void createsPaymentForEligiblePlayerAndRecordsMonth() {
        when(generationRepository.existsByTenantIdAndMois(1L, month.toString())).thenReturn(false);
        when(joueurRepository.findAll()).thenReturn(List.of(eligiblePlayer()));
        when(paiementRepository.findByJoueurIdAndDateEcheanceBetween(eq(1L), any(), any()))
                .thenReturn(List.of());

        assertEquals(1, service().ensureMonthlyPayments(1L, month));

        verify(paiementRepository).save(argThat(p ->
                p.getStatut() == StatutPaiement.EN_ATTENTE
                        && p.getDateEcheance().equals(month.atDay(1))));
        verify(generationRepository).save(argThat(g ->
                g.getTenantId().equals(1L) && g.getMois().equals(month.toString())));
    }

    @Test
    void skipsIneligiblePlayers() {
        Joueur noParent = eligiblePlayer();
        noParent.setParent(null);
        Joueur noEntry = eligiblePlayer();
        noEntry.setId(2L);
        noEntry.setDateEntree(null);
        Joueur future = eligiblePlayer();
        future.setId(3L);
        future.setDateEntree(LocalDate.now().plusMonths(1));
        Joueur noFrequency = eligiblePlayer();
        noFrequency.setId(4L);
        noFrequency.setFrequence(null);
        Joueur alreadyBilled = eligiblePlayer();
        alreadyBilled.setId(5L);
        when(generationRepository.existsByTenantIdAndMois(1L, month.toString())).thenReturn(false);
        when(joueurRepository.findAll())
                .thenReturn(List.of(noParent, noEntry, future, noFrequency, alreadyBilled));
        when(paiementRepository.findByJoueurIdAndDateEcheanceBetween(eq(5L), any(), any()))
                .thenReturn(List.of(Paiement.builder().statut(StatutPaiement.EN_ATTENTE).build()));

        assertEquals(0, service().ensureMonthlyPayments(1L, month));

        verify(paiementRepository, never()).save(any());
        verify(generationRepository).save(any());
    }

    @Test
    void concurrentRaceKeepsCreatedCount() {
        when(generationRepository.existsByTenantIdAndMois(1L, month.toString())).thenReturn(false);
        when(joueurRepository.findAll()).thenReturn(List.of(eligiblePlayer()));
        when(paiementRepository.findByJoueurIdAndDateEcheanceBetween(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(generationRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertEquals(1, service().ensureMonthlyPayments(1L, month));
    }
}
