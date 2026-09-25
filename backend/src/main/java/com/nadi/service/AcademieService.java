package com.nadi.service;

import com.nadi.dto.AcademieRequest;
import com.nadi.dto.AcademieResponse;
import com.nadi.dto.TenantPublicResponse;
import com.nadi.model.Academie;
import com.nadi.model.Abonnement;
import com.nadi.model.Categorie;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.AbonnementRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.SecurityUtils;
import com.nadi.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademieService {

    private final AcademieRepository academieRepository;
    private final AbonnementRepository abonnementRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<AcademieResponse> listAll(Pageable pageable) {
        return academieRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Public academy list for the login selector. Hit on every login screen
     * visit but changes rarely, so it is cached; all academy mutations evict it.
     */
    @Cacheable("publicTenants")
    @Transactional(readOnly = true)
    public java.util.List<TenantPublicResponse> getActiveTenants() {
        return academieRepository.findAll().stream()
                .filter(Academie::getActive)
                .map(a -> TenantPublicResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom())
                        .slug(a.getSlug())
                        .ville(a.getVille())
                        .logoUrl(a.getLogoUrl())
                        .build())
                .toList();
    }

    /**
     * Cross-tenant IDOR guard: only SUPER_ADMIN may touch any academy.
     * Other roles are restricted to their own tenant academy.
     */
    private void requireAcademyAccess(Long id) {
        if (securityUtils.isSuperAdmin()) {
            return;
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || !tenantId.equals(id)) {
            throw new AccessDeniedException("Accès interdit à cette académie");
        }
    }

    @Transactional(readOnly = true)
    public AcademieResponse getById(Long id) {
        requireAcademyAccess(id);
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));
        return toResponse(academie);
    }

    @CacheEvict(value = "publicTenants", allEntries = true)
    @Transactional
    public AcademieResponse create(AcademieRequest request) {
        if (academieRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Ce slug est déjà utilisé: " + request.getSlug());
        }

        Academie academie = Academie.builder()
                .slug(request.getSlug())
                .nom(request.getNom())
                .logoUrl(request.getLogoUrl())
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .active(request.getActive() != null ? request.getActive() : true)
                .dateActivation(request.getDateActivation() != null ? request.getDateActivation() : LocalDate.now())
                .dateExpiration(request.getDateExpiration())
                .plan(request.getPlan() != null ? request.getPlan() : "FREE")
                .build();

        academie = academieRepository.save(academie);

        // Create admin user if credentials provided
        if (request.getAdminEmail() != null && !request.getAdminEmail().isBlank()
                && request.getAdminMotDePasse() != null && !request.getAdminMotDePasse().isBlank()) {
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
            log.info("Created admin user {} for academy {}", request.getAdminEmail(), academie.getSlug());
        }

        return toResponse(academie);
    }

    @CacheEvict(value = "publicTenants", allEntries = true)
    @Transactional
    public AcademieResponse update(Long id, AcademieRequest request) {
        requireAcademyAccess(id);
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        if (request.getNom() != null) academie.setNom(request.getNom());
        if (request.getLogoUrl() != null) academie.setLogoUrl(request.getLogoUrl());
        if (request.getAdresse() != null) academie.setAdresse(request.getAdresse());
        if (request.getVille() != null) academie.setVille(request.getVille());
        if (request.getTelephone() != null) academie.setTelephone(request.getTelephone());
        if (request.getEmail() != null) academie.setEmail(request.getEmail());
        if (request.getActive() != null) academie.setActive(request.getActive());
        if (request.getDateActivation() != null) academie.setDateActivation(request.getDateActivation());
        if (request.getDateExpiration() != null) academie.setDateExpiration(request.getDateExpiration());
        if (request.getPlan() != null) academie.setPlan(request.getPlan());

        return toResponse(academieRepository.save(academie));
    }

    @CacheEvict(value = "publicTenants", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        Long tenantId = academie.getId();
        log.info("Deleting academie id={}, slug={}, tenantId={}", id, academie.getSlug(), tenantId);

        TenantContext.clear();

        String[] tablesInOrder = {
            "consent_log", "consentement_rgpd", "device_token", "notification", "invitation",
            "note_joueur", "absence", "paiement", "convocation", "document", "facture",
            "entraineur", "parent", "joueur", "creneau", "categorie",
            "abonnement", "evenement", "audit_log", "utilisateur"
        };

        for (String table : tablesInOrder) {
            int deleted = entityManager.createNativeQuery("DELETE FROM " + table + " WHERE tenant_id = ?1")
                    .setParameter(1, tenantId)
                    .executeUpdate();
            if (deleted > 0) {
                log.info("Deleted {} rows from {}", deleted, table);
            }
        }

        academieRepository.deleteById(id);
        log.info("Academie id={} deleted successfully", id);
    }

    @CacheEvict(value = "publicTenants", allEntries = true)
    @Transactional
    public AcademieResponse toggleActive(Long id) {
        requireAcademyAccess(id);
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        academie.setActive(!academie.getActive());
        if (academie.getActive() && academie.getDateActivation() == null) {
            academie.setDateActivation(LocalDate.now());
        }

        return toResponse(academieRepository.save(academie));
    }

    @CacheEvict(value = "publicTenants", allEntries = true)
    @Transactional
    public AcademieResponse deactivateIfUnpaid(Long id) {
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        Long previousTenantId = TenantContext.getTenantId();
        try {
            TenantContext.clear();
            boolean hasActiveSubscription = abonnementRepository
                    .findByTenantIdAndStatut(academie.getId(), Abonnement.StatutAbonnement.ACTIF)
                    .isPresent();

            if (!hasActiveSubscription) {
                academie.setActive(false);
                log.info("Academie id={} deactivated: no active subscription", id);
            } else {
                log.info("Academie id={} has active subscription, not deactivating", id);
            }
        } finally {
            if (previousTenantId != null) {
                TenantContext.setTenantId(previousTenantId);
            }
        }

        return toResponse(academieRepository.save(academie));
    }

    @Transactional(readOnly = true)
    public AcademieResponse getCurrentTenantInfo() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Aucun contexte tenant disponible");
        }
        return getById(tenantId);
    }

    private AcademieResponse toResponse(Academie academie) {
        Long joueurCount = 0L;
        Long parentCount = 0L;
        Long coachCount = 0L;

        try {
            joueurCount = joueurRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        try {
            parentCount = parentRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        try {
            coachCount = entraineurRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        return AcademieResponse.builder()
                .id(academie.getId())
                .slug(academie.getSlug())
                .nom(academie.getNom())
                .logoUrl(academie.getLogoUrl())
                .adresse(academie.getAdresse())
                .ville(academie.getVille())
                .telephone(academie.getTelephone())
                .email(academie.getEmail())
                .active(academie.getActive())
                .dateActivation(academie.getDateActivation())
                .dateExpiration(academie.getDateExpiration())
                .plan(academie.getPlan())
                .joueurCount(joueurCount)
                .parentCount(parentCount)
                .coachCount(coachCount)
                .createdAt(academie.getCreatedAt())
                .updatedAt(academie.getUpdatedAt())
                .build();
    }

    private void createDefaultCategories(Long tenantId) {
        categorieRepository.save(Categorie.builder().nom("U9").description("Formation").ageMin(6).ageMax(9).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("U13").description("Perfectionnement").ageMin(10).ageMax(13).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("U17").description("Pre-nationale").ageMin(14).ageMax(17).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("Elite").description("Selection").ageMin(15).ageMax(19).tenantId(tenantId).build());
        categorieRepository.save(Categorie.builder().nom("Generaux").description("Categorie generale").ageMin(6).ageMax(19).tenantId(tenantId).build());
    }
}
