package com.nadi.service;

import com.nadi.dto.AcceptInvitationRequest;
import com.nadi.dto.InvitationRequest;
import com.nadi.dto.InvitationResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.security.SecurityUtils;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Transactional
    public InvitationResponse createInvitation(InvitationRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Aucun contexte tenant disponible");
        }

        // Deliberately identical messages: distinct responses would let callers
        // enumerate which emails already have accounts or pending invitations.
        if (invitationRepository.existsByEmailAndStatut(request.getEmail(), Invitation.StatutInvitation.EN_ATTENTE)
                || utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Impossible de créer une invitation pour cet email");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + request.getRole());
        }

        // Privilege-escalation guard: an inviter can only grant roles
        // strictly below their own. Only SUPER_ADMIN may invite ADMIN
        // or SUPER_ADMIN.
        Role inviterRole = securityUtils.getCurrentUserOrThrow().getRole();
        if (!isRoleGrantableBy(inviterRole, role)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez pas inviter avec le rôle: " + request.getRole());
        }

        Utilisateur currentUser = securityUtils.getCurrentUserOrThrow();

        Invitation invitation = Invitation.builder()
                .token(UUID.randomUUID().toString())
                .email(request.getEmail())
                .role(role)
                .invitedBy(currentUser)
                .statut(Invitation.StatutInvitation.EN_ATTENTE)
                .dateExpiration(LocalDateTime.now().plusDays(7))
                .tenantId(tenantId)
                .build();

        Invitation saved = invitationRepository.save(invitation);
        return toResponse(saved);
    }

    private static boolean isRoleGrantableBy(Role inviterRole, Role grantedRole) {
        if (inviterRole == Role.SUPER_ADMIN) {
            return true;
        }
        if (inviterRole == Role.ADMIN) {
            return EnumSet.of(Role.COACH, Role.PARENT).contains(grantedRole);
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listPending() {
        return invitationRepository.findByStatut(Invitation.StatutInvitation.EN_ATTENTE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listAll() {
        return invitationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvitationResponse getByToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        if (invitation.getDateExpiration().isBefore(LocalDateTime.now())) {
            invitation.setStatut(Invitation.StatutInvitation.EXPIREE);
            invitationRepository.save(invitation);
            throw new RuntimeException("Cette invitation a expiré");
        }

        if (invitation.getStatut() != Invitation.StatutInvitation.EN_ATTENTE) {
            throw new RuntimeException("Cette invitation n'est plus valide");
        }

        return toResponse(invitation);
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        if (invitation.getDateExpiration().isBefore(LocalDateTime.now())) {
            invitation.setStatut(Invitation.StatutInvitation.EXPIREE);
            invitationRepository.save(invitation);
            throw new RuntimeException("Cette invitation a expiré");
        }

        if (invitation.getStatut() != Invitation.StatutInvitation.EN_ATTENTE) {
            throw new RuntimeException("Cette invitation n'est plus valide");
        }

        com.nadi.security.PasswordPolicy.validateOrThrow(request.getMotDePasse());

        TenantContext.setTenantId(invitation.getTenantId());

        Utilisateur user = Utilisateur.builder()
                .email(invitation.getEmail())
                .motDePasseHash(passwordEncoder.encode(request.getMotDePasse()))
                .role(invitation.getRole())
                .actif(true)
                .mustChangePassword(false)
                .tenantId(invitation.getTenantId())
                .build();
        Utilisateur savedUser = utilisateurRepository.save(user);

        if (invitation.getRole() == Role.PARENT) {
            Parent parent = Parent.builder()
                    .prenom(request.getPrenom())
                    .nom(request.getNom())
                    .email(invitation.getEmail())
                    .utilisateur(savedUser)
                    .tenantId(invitation.getTenantId())
                    .build();
            parentRepository.save(parent);
        } else if (invitation.getRole() == Role.COACH) {
            Entraineur coach = Entraineur.builder()
                    .prenom(request.getPrenom())
                    .nom(request.getNom())
                    .email(invitation.getEmail())
                    .utilisateur(savedUser)
                    .tenantId(invitation.getTenantId())
                    .build();
            entraineurRepository.save(coach);
        }

        invitation.setStatut(Invitation.StatutInvitation.ACCEPTEE);
        invitation.setDateAcceptation(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(Long id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        if (invitation.getStatut() != Invitation.StatutInvitation.EN_ATTENTE) {
            throw new RuntimeException("Seules les invitations en attente peuvent être annulées");
        }

        invitation.setStatut(Invitation.StatutInvitation.ANNULEE);
        invitationRepository.save(invitation);
    }

    private InvitationResponse toResponse(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .token(invitation.getToken())
                .email(invitation.getEmail())
                .role(invitation.getRole().name())
                .invitedByEmail(invitation.getInvitedBy() != null ? invitation.getInvitedBy().getEmail() : null)
                .statut(invitation.getStatut().name())
                .dateAcceptation(invitation.getDateAcceptation())
                .dateExpiration(invitation.getDateExpiration())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
