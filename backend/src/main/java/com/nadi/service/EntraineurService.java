package com.nadi.service;

import com.nadi.dto.CategorieResponse;
import com.nadi.dto.EntraineurRequest;
import com.nadi.dto.EntraineurResponse;
import com.nadi.model.Categorie;
import com.nadi.model.Entraineur;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntraineurService {

    private final EntraineurRepository entraineurRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<EntraineurResponse> getAll(Pageable pageable) {
        return entraineurRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EntraineurResponse> search(String query, Pageable pageable) {
        return entraineurRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(query, query, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EntraineurResponse getById(Long id) {
        Entraineur entraineur = entraineurRepository.findByIdWithCategories(id)
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + id));
        return toResponse(entraineur);
    }

    @Transactional(readOnly = true)
    public List<EntraineurResponse> getAllList() {
        return entraineurRepository.findAllWithCategories().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EntraineurResponse create(EntraineurRequest request) {
        Set<Categorie> categories = new HashSet<>();
        if (request.getCategorieIds() != null) {
            categories = request.getCategorieIds().stream()
                    .map(id -> categorieRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + id)))
                    .collect(Collectors.toSet());
        }

        Entraineur entraineur = Entraineur.builder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .specialite(request.getSpecialite())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .photoUrl(request.getPhotoUrl())
                .categories(categories)
                .build();
        entraineur = entraineurRepository.save(entraineur);

        String motDePasse = null;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            motDePasse = "Nadi" + UUID.randomUUID().toString().substring(0, 4);
            Utilisateur utilisateur = Utilisateur.builder()
                    .email(request.getEmail())
                    .motDePasseHash(passwordEncoder.encode(motDePasse))
                    .role(Role.COACH)
                    .actif(true)
                    .build();
            utilisateur = utilisateurRepository.save(utilisateur);
            entraineur.setUtilisateur(utilisateur);
            entraineur = entraineurRepository.save(entraineur);
        }

        EntraineurResponse response = toResponse(entraineur);
        response.setMotDePasse(motDePasse);
        return response;
    }

    @Transactional
    public EntraineurResponse update(Long id, EntraineurRequest request) {
        Entraineur entraineur = entraineurRepository.findByIdWithCategories(id)
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + id));

        entraineur.setPrenom(request.getPrenom());
        entraineur.setNom(request.getNom());
        entraineur.setSpecialite(request.getSpecialite());
        entraineur.setTelephone(request.getTelephone());
        entraineur.setEmail(request.getEmail());
        entraineur.setPhotoUrl(request.getPhotoUrl());

        if (request.getCategorieIds() != null) {
            Set<Categorie> categories = request.getCategorieIds().stream()
                    .map(catId -> categorieRepository.findById(catId)
                            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + catId)))
                    .collect(Collectors.toSet());
            entraineur.setCategories(categories);
        }

        return toResponse(entraineurRepository.save(entraineur));
    }

    @Transactional
    public void delete(Long id) {
        Entraineur entraineur = entraineurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entraîneur non trouvé: " + id));
        if (!entraineur.getCreneaux().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer: l'entraîneur a des créneaux actifs. Réaffectez-les d'abord.");
        }
        entraineurRepository.deleteById(id);
    }

    private EntraineurResponse toResponse(Entraineur e) {
        List<CategorieResponse> cats = e.getCategories() != null
                ? e.getCategories().stream().map(c -> CategorieResponse.builder()
                        .id(c.getId()).nom(c.getNom()).description(c.getDescription())
                        .ageMin(c.getAgeMin()).ageMax(c.getAgeMax()).build())
                        .collect(Collectors.toList())
                : List.of();

        return EntraineurResponse.builder()
                .id(e.getId())
                .prenom(e.getPrenom())
                .nom(e.getNom())
                .specialite(e.getSpecialite())
                .telephone(e.getTelephone())
                .email(e.getEmail())
                .photoUrl(e.getPhotoUrl())
                .categories(cats)
                .utilisateurId(e.getUtilisateur() != null ? e.getUtilisateur().getId() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
