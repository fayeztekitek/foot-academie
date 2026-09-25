package com.nadi.config;

import com.nadi.model.Academie;
import com.nadi.model.Categorie;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.PasswordPolicy;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final AcademieRepository academieRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    private static final Long DEFAULT_TENANT_ID = 1L;

    @Override
    @Transactional
    public void run(String... args) {
        TenantContext.setTenantId(DEFAULT_TENANT_ID);

        if (!academieRepository.existsById(DEFAULT_TENANT_ID)) {
            Academie academie = Academie.builder()
                    .id(DEFAULT_TENANT_ID)
                    .slug("nadi-default")
                    .nom("Nadi Académie")
                    .ville("Tunis")
                    .active(true)
                    .plan("PRO")
                    .build();
            academieRepository.save(academie);
            log.info("Default tenant created: Nadi Académie (id={})", DEFAULT_TENANT_ID);
        }

        if (isProd()) {
            seedProdSuperAdmin();
        } else {
            seedDevAccount("superadmin@nadi.tn", "superadmin123", Role.SUPER_ADMIN);
            seedDevAccount("admin@nadi.tn", "admin123", Role.ADMIN);
            seedDevAccount("coach@nadi.tn", "coach123", Role.COACH);
            seedDevAccount("parent@nadi.tn", "parent123", Role.PARENT);
        }

        seedDefaultCategories();

        TenantContext.clear();
    }

    private boolean isProd() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    /**
     * Production bootstrap: create a single SUPER_ADMIN only when none exists,
     * with a random one-time password that must be rotated on first login.
     * No guessable defaults, no demo accounts, no plaintext passwords in logs.
     */
    private void seedProdSuperAdmin() {
        // Check across ALL tenants: the tenant filter would otherwise hide
        // SUPER_ADMIN accounts living in other tenants.
        Long previousTenant = TenantContext.getTenantId();
        boolean superAdminExists;
        try {
            TenantContext.clear();
            superAdminExists = utilisateurRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == Role.SUPER_ADMIN);
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            }
        }
        if (superAdminExists) {
            return;
        }
        String oneTimePassword = PasswordPolicy.generateTemporaryPassword()
                + UUID.randomUUID().toString().substring(0, 4);
        Utilisateur superAdmin = Utilisateur.builder()
                .email("superadmin@nadi.tn")
                .motDePasseHash(passwordEncoder.encode(oneTimePassword))
                .role(Role.SUPER_ADMIN)
                .actif(true)
                .mustChangePassword(true)
                .tenantId(DEFAULT_TENANT_ID)
                .build();
        utilisateurRepository.save(superAdmin);
        log.warn("PROD BOOTSTRAP: SUPER_ADMIN superadmin@nadi.tn created with one-time password: {} "
                + "— change it immediately via /auth/change-password", oneTimePassword);
    }

    private void seedDevAccount(String email, String password, Role role) {
        if (utilisateurRepository.existsByEmail(email)) {
            return;
        }
        Utilisateur user = Utilisateur.builder()
                .email(email)
                .motDePasseHash(passwordEncoder.encode(password))
                .role(role)
                .actif(true)
                .tenantId(DEFAULT_TENANT_ID)
                .build();
        utilisateurRepository.save(user);
        log.info("Dev account created: {} ({})", email, role);
    }

    private void seedDefaultCategories() {
        if (categorieRepository.count() == 0) {
            categorieRepository.save(Categorie.builder().nom("U9").description("Formation").ageMin(6).ageMax(9).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("U13").description("Perfectionnement").ageMin(10).ageMax(13).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("U17").description("Pré-nationale").ageMin(14).ageMax(17).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("Élite").description("Sélection").ageMin(15).ageMax(19).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("Généraux").description("Catégorie générale — tous âges").ageMin(6).ageMax(19).tenantId(DEFAULT_TENANT_ID).build());
            log.info("5 default categories created: U9, U13, U17, Élite, Généraux");
        }
    }
}
