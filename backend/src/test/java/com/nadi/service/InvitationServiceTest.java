package com.nadi.service;

import com.nadi.dto.AcceptInvitationRequest;
import com.nadi.dto.InvitationRequest;
import com.nadi.model.Invitation;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.InvitationRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.SecurityUtils;
import com.nadi.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private InvitationService invitationService;

    @BeforeEach
    void setUp() {
        invitationService = new InvitationService(
                invitationRepository, utilisateurRepository, parentRepository,
                entraineurRepository, passwordEncoder, new SecurityUtils(utilisateurRepository));
        TenantContext.setTenantId(5L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Role role) {
        Utilisateur user = Utilisateur.builder()
                .id(1L).email("boss@nadi.tn").motDePasseHash("hash")
                .role(role).actif(true).tenantId(5L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }

    private InvitationRequest request(String email, String role) {
        InvitationRequest request = new InvitationRequest();
        request.setEmail(email);
        request.setRole(role);
        return request;
    }

    @Test
    void adminCanInviteCoach() {
        authenticateAs(Role.ADMIN);
        when(invitationRepository.existsByEmailAndStatut(any(), any())).thenReturn(false);
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        invitationService.createInvitation(request("coach@nadi.tn", "COACH"));

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertEquals(Role.COACH, captor.getValue().getRole());
        assertEquals(5L, captor.getValue().getTenantId());
    }

    @Test
    void adminCannotInviteSuperAdmin() {
        authenticateAs(Role.ADMIN);
        when(invitationRepository.existsByEmailAndStatut(any(), any())).thenReturn(false);
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> invitationService.createInvitation(request("evil@nadi.tn", "SUPER_ADMIN")));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void adminCannotInviteAdmin() {
        authenticateAs(Role.ADMIN);
        when(invitationRepository.existsByEmailAndStatut(any(), any())).thenReturn(false);
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> invitationService.createInvitation(request("peer@nadi.tn", "ADMIN")));
    }

    @Test
    void superAdminCanInviteAdmin() {
        authenticateAs(Role.SUPER_ADMIN);
        when(invitationRepository.existsByEmailAndStatut(any(), any())).thenReturn(false);
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(invitationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        invitationService.createInvitation(request("admin2@nadi.tn", "ADMIN"));

        verify(invitationRepository).save(any());
    }

    @Test
    void parentCannotInviteAnyone() {
        authenticateAs(Role.PARENT);
        when(invitationRepository.existsByEmailAndStatut(any(), any())).thenReturn(false);
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> invitationService.createInvitation(request("x@nadi.tn", "PARENT")));
    }

    @Test
    void acceptRejectsWeakPassword() {
        Invitation invitation = Invitation.builder()
                .token("tok").email("coach@nadi.tn").role(Role.COACH)
                .statut(Invitation.StatutInvitation.EN_ATTENTE)
                .dateExpiration(LocalDateTime.now().plusDays(7))
                .tenantId(5L).build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));

        AcceptInvitationRequest request = AcceptInvitationRequest.builder()
                .token("tok").nom("N").prenom("P").motDePasse("short").build();

        assertThrows(RuntimeException.class, () -> invitationService.acceptInvitation(request));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void acceptCreatesUserWithInvitedRole() {
        Invitation invitation = Invitation.builder()
                .token("tok").email("coach@nadi.tn").role(Role.COACH)
                .statut(Invitation.StatutInvitation.EN_ATTENTE)
                .dateExpiration(LocalDateTime.now().plusDays(7))
                .tenantId(5L).build();
        when(invitationRepository.findByToken("tok")).thenReturn(Optional.of(invitation));
        when(passwordEncoder.encode("StrongPass1")).thenReturn("hash");
        when(utilisateurRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AcceptInvitationRequest request = AcceptInvitationRequest.builder()
                .token("tok").nom("N").prenom("P").motDePasse("StrongPass1").build();

        invitationService.acceptInvitation(request);

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertEquals(Role.COACH, captor.getValue().getRole());
        assertEquals(5L, captor.getValue().getTenantId());
    }
}
