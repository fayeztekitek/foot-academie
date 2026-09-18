package com.nadi.service;

import com.nadi.dto.JoueurResponse;
import com.nadi.dto.ParentRequest;
import com.nadi.dto.ParentResponse;
import com.nadi.model.Joueur;
import com.nadi.model.Parent;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Page<ParentResponse> getAll(Pageable pageable) {
        return parentRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ParentResponse> search(String query, Pageable pageable) {
        return parentRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(query, query, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ParentResponse getById(Long id) {
        Parent parent = parentRepository.findByIdWithJoueurs(id)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + id));
        return toResponse(parent);
    }

    @Transactional(readOnly = true)
    public ParentResponse getByUtilisateurId(Long utilisateurId) {
        Parent parent = parentRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé pour l'utilisateur: " + utilisateurId));
        return toResponse(parent);
    }

    @Transactional
    public ParentResponse create(ParentRequest request) {
        Parent parent = Parent.builder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .consentementRGPD(request.getConsentementRGPD() != null ? request.getConsentementRGPD() : false)
                .dateConsentementRGPD(request.getConsentementRGPD() != null && request.getConsentementRGPD() ? LocalDate.now() : null)
                .build();
        parent = parentRepository.save(parent);

        String motDePasse = null;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            motDePasse = (request.getMotDePasse() != null && !request.getMotDePasse().isBlank())
                    ? request.getMotDePasse()
                    : "Nadi" + UUID.randomUUID().toString().substring(0, 4);
            Utilisateur utilisateur = Utilisateur.builder()
                    .email(request.getEmail())
                    .motDePasseHash(passwordEncoder.encode(motDePasse))
                    .role(Role.PARENT)
                    .actif(true)
                    .mustChangePassword(true)
                    .build();
            utilisateur = utilisateurRepository.save(utilisateur);
            parent.setUtilisateur(utilisateur);
            parent = parentRepository.save(parent);
        }

        ParentResponse response = toResponse(parent);
        response.setMotDePasse(motDePasse);

        if (motDePasse != null && request.getEmail() != null && !request.getEmail().isBlank()) {
            emailService.sendParentCredentials(
                request.getEmail(),
                request.getPrenom(),
                request.getNom(),
                request.getEmail(),
                motDePasse
            );
        }

        return response;
    }

    @Transactional
    public ParentResponse update(Long id, ParentRequest request) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + id));
        parent.setPrenom(request.getPrenom());
        parent.setNom(request.getNom());
        parent.setTelephone(request.getTelephone());
        parent.setEmail(request.getEmail());
        if (request.getConsentementRGPD() != null && request.getConsentementRGPD() && !parent.getConsentementRGPD()) {
            parent.setConsentementRGPD(true);
            parent.setDateConsentementRGPD(LocalDate.now());
        }
        return toResponse(parentRepository.save(parent));
    }

    @Transactional
    public void resetPassword(Long id, String nouveauMotDePasse) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + id));

        if (parent.getUtilisateur() == null) {
            if (parent.getEmail() == null || parent.getEmail().isBlank()) {
                throw new RuntimeException("Ce parent n'a pas d'email configuré, impossible de créer un compte");
            }
            Utilisateur utilisateur = Utilisateur.builder()
                    .email(parent.getEmail())
                    .motDePasseHash(passwordEncoder.encode(nouveauMotDePasse))
                    .role(Role.PARENT)
                    .actif(true)
                    .mustChangePassword(true)
                    .build();
            utilisateur = utilisateurRepository.save(utilisateur);
            parent.setUtilisateur(utilisateur);
            parentRepository.save(parent);
        } else {
            parent.getUtilisateur().setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
            parent.getUtilisateur().setMustChangePassword(true);
            utilisateurRepository.save(parent.getUtilisateur());
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!parentRepository.existsById(id)) {
            throw new RuntimeException("Parent non trouvé: " + id);
        }
        parentRepository.deleteById(id);
    }

    private ParentResponse toResponse(Parent p) {
        List<JoueurResponse> joueurs = p.getJoueurs() != null
                ? p.getJoueurs().stream().map(this::toJoueurResponse).collect(Collectors.toList())
                : List.of();

        return ParentResponse.builder()
                .id(p.getId())
                .prenom(p.getPrenom())
                .nom(p.getNom())
                .telephone(p.getTelephone())
                .email(p.getEmail())
                .consentementRGPD(p.getConsentementRGPD())
                .dateConsentementRGPD(p.getDateConsentementRGPD())
                .utilisateurId(p.getUtilisateur() != null ? p.getUtilisateur().getId() : null)
                .joueurs(joueurs)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private JoueurResponse toJoueurResponse(Joueur j) {
        return JoueurResponse.builder()
                .id(j.getId())
                .prenom(j.getPrenom())
                .nom(j.getNom())
                .dateNaissance(j.getDateNaissance())
                .categorieId(j.getCategorie() != null ? j.getCategorie().getId() : null)
                .categorieNom(j.getCategorie() != null ? j.getCategorie().getNom() : null)
                .statutPaiement(j.getStatutPaiement().name())
                .photoUrl(j.getPhotoUrl())
                .build();
    }
}
