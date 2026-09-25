package com.nadi.service;

import com.nadi.dto.JoueurRequest;
import com.nadi.dto.JoueurResponse;
import com.nadi.model.Joueur;
import com.nadi.model.Parent;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.PaiementRepository;
import com.nadi.repository.ParentRepository;
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
class JoueurServiceTest {

    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private PaiementService paiementService;
    @Mock
    private PaiementRepository paiementRepository;

    private JoueurService service() {
        return new JoueurService(joueurRepository, categorieRepository,
                parentRepository, paiementService, paiementRepository);
    }

    private JoueurRequest request() {
        JoueurRequest request = new JoueurRequest();
        request.setPrenom("Amine");
        request.setNom("Zouari");
        request.setDateNaissance(LocalDate.of(2015, 10, 30));
        return request;
    }

    @Test
    void createPlayerDefaultsToAJour() {
        when(joueurRepository.save(any())).thenAnswer(i -> {
            Joueur j = i.getArgument(0);
            j.setId(1L);
            return j;
        });
        when(paiementRepository.findByJoueurId(1L)).thenReturn(List.of());

        JoueurResponse response = service().create(request());

        assertEquals("Amine", response.getPrenom());
        assertEquals("A_JOUR", response.getStatutPaiement());
    }

    @Test
    void responseSurvivesNullStatutPaiement() {
        // Regression test: legacy rows with NULL statut_paiement used to
        // throw NullPointerException (HTTP 500) on update/read.
        Joueur joueur = Joueur.builder().prenom("A").nom("Z")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(2L);
        joueur.setStatutPaiement(null);
        when(joueurRepository.findByIdWithDetails(2L)).thenReturn(joueur);
        when(joueurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JoueurResponse response = service().update(2L, request());

        assertEquals("A_JOUR", response.getStatutPaiement());
    }

    @Test
    void updateWithNullParentClearsAssociation() {
        Joueur joueur = Joueur.builder().prenom("A").nom("Z")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(3L);
        joueur.setParent(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build());
        when(joueurRepository.findByIdWithDetails(3L)).thenReturn(joueur);
        when(joueurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JoueurRequest update = request();
        update.setParentId(null);

        JoueurResponse response = service().update(3L, update);

        assertNull(response.getParentId());
    }

    @Test
    void updateRejectsInvalidPostePrincipal() {
        Joueur joueur = Joueur.builder().prenom("A").nom("Z")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(4L);
        when(joueurRepository.findByIdWithDetails(4L)).thenReturn(joueur);

        JoueurRequest update = request();
        update.setPostePrincipal("GOALKEEPER");

        assertThrows(IllegalArgumentException.class, () -> service().update(4L, update));
    }

    @Test
    void updateMissingPlayerThrows() {
        when(joueurRepository.findByIdWithDetails(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service().update(999L, request()));
    }

    @Test
    void deleteMissingPlayerThrows() {
        when(joueurRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service().delete(999L));
        verify(joueurRepository, never()).deleteById(any());
    }

    @Test
    void changeFrequenceRecalculatesPayments() {
        Joueur joueur = Joueur.builder().prenom("A").nom("Z")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(5L);
        when(joueurRepository.findByIdWithDetails(5L)).thenReturn(joueur);
        when(joueurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JoueurResponse response = service().changeFrequence(5L, "MENSUEL");

        assertEquals("MENSUEL", response.getFrequence());
        verify(paiementService).recalculateFuturePayments(eq(5L), any());
    }
}
