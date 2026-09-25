package com.nadi.service;

import com.nadi.dto.ConvocationResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvenementServiceTest {

    @Mock
    private EvenementRepository evenementRepository;
    @Mock
    private ConvocationRepository convocationRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private EvenementService evenementService;

    @BeforeEach
    void setUp() {
        evenementService = new EvenementService(
                evenementRepository, convocationRepository, joueurRepository, parentRepository,
                notificationRepository, utilisateurRepository, new SecurityUtils(utilisateurRepository));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Utilisateur parentUser(Long userId) {
        return Utilisateur.builder()
                .id(userId).email("p@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
    }

    private void authenticate(Utilisateur user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
    }

    private Parent parent(Long id, Utilisateur user) {
        Parent parent = Parent.builder().id(id).prenom("P").nom("N").email("p@nadi.tn").build();
        parent.setUtilisateur(user);
        return parent;
    }

    private Convocation convocation(Long parentId, Utilisateur user) {
        Joueur joueur = Joueur.builder().prenom("J").nom("N")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(1L);
        joueur.setParent(parent(parentId, user));
        Evenement event = Evenement.builder().titre("Tournoi")
                .typeEvenement(Evenement.TypeEvenement.TOURNOI)
                .dateDebut(LocalDate.now()).lieu("Tunis").build();
        event.setId(1L);
        return Convocation.builder().id(1L).evenement(event).joueur(joueur)
                .parent(parent(parentId, user))
                .statut(Convocation.StatutConvocation.INVITE).build();
    }

    @Test
    void parentCanRespondToOwnConvocation() {
        Utilisateur user = parentUser(42L);
        authenticate(user);
        Convocation convocation = convocation(10L, user);
        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));
        when(convocationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ConvocationResponse response = evenementService.respondToConvocation(1L, true);

        assertEquals("CONFIRME", response.getStatut());
    }

    @Test
    void parentCannotRespondToOtherFamilyConvocation() {
        Utilisateur user = parentUser(42L);
        authenticate(user);
        Utilisateur stranger = parentUser(99L);
        Convocation convocation = convocation(11L, stranger);
        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));

        assertThrows(AccessDeniedException.class,
                () -> evenementService.respondToConvocation(1L, true));
        verify(convocationRepository, never()).save(any());
    }

    @Test
    void respondToMissingConvocationThrows() {
        authenticate(parentUser(42L));
        when(convocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evenementService.respondToConvocation(999L, true));
    }

    @Test
    void parentCanRefuseOwnConvocation() {
        Utilisateur user = parentUser(42L);
        authenticate(user);
        Convocation convocation = convocation(10L, user);
        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));
        when(convocationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ConvocationResponse response = evenementService.respondToConvocation(1L, false);

        assertEquals("REFUSE", response.getStatut());
        assertNotNull(response.getDateReponse());
    }

    @Test
    void myConvocationsAreScopedToCaller() {
        Utilisateur user = parentUser(42L);
        authenticate(user);
        when(convocationRepository.findByParentUserId(42L)).thenReturn(List.of(convocation(10L, user)));

        List<ConvocationResponse> responses = evenementService.getMyConvocations();

        assertEquals(1, responses.size());
        verify(convocationRepository).findByParentUserId(42L);
    }
}
