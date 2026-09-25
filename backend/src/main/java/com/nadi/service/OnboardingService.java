package com.nadi.service;

import com.nadi.dto.OnboardingRequest;
import com.nadi.dto.OnboardingResponse;
import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final AcademieRepository academieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OnboardingResponse onboard(OnboardingRequest request) {
        String slug = generateSlug(request.getAcademieNom());

        if (academieRepository.existsBySlug(slug)) {
            throw new RuntimeException("Une académie avec ce nom existe déjà");
        }

        Academie academie = Academie.builder()
                .slug(slug)
                .nom(request.getAcademieNom())
                .ville(request.getAcademieVille())
                .adresse(request.getAcademieAdresse())
                .telephone(request.getAcademieTelephone())
                .active(true)
                .dateActivation(java.time.LocalDate.now())
                .plan("FREE")
                .build();
        academie = academieRepository.save(academie);

        com.nadi.security.PasswordPolicy.validateOrThrow(request.getAdminMotDePasse());

        TenantContext.setTenantId(academie.getId());

        Utilisateur admin = Utilisateur.builder()
                .email(request.getAdminEmail())
                .motDePasseHash(passwordEncoder.encode(request.getAdminMotDePasse()))
                .role(Role.ADMIN)
                .actif(true)
                .tenantId(academie.getId())
                .build();
        utilisateurRepository.save(admin);

        createDefaultCategories(academie.getId());

        return OnboardingResponse.builder()
                .academieId(academie.getId())
                .academieSlug(academie.getSlug())
                .academieNom(academie.getNom())
                .adminEmail(request.getAdminEmail())
                .message("Académie créée avec succès. Connectez-vous avec votre email et mot de passe.")
                .build();
    }

    private void createDefaultCategories(Long tenantId) {
        categorieRepository.save(Categorie.builder().nom("U9").description("Formation").ageMin(6).ageMax(9).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("U13").description("Perfectionnement").ageMin(10).ageMax(13).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("U17").description("Pre-nationale").ageMin(14).ageMax(17).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("Elite").description("Selection").ageMin(15).ageMax(19).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("Generaux").description("Categorie generale").ageMin(6).ageMax(19).tenantId(tenantId).build());
    }

    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\p{L}\\p{Nd}]+");
        String slug = pattern.matcher(normalized).replaceAll("-").toLowerCase();
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }
}
