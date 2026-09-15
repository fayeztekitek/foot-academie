package com.nadi.config;

import com.nadi.model.Academie;
import com.nadi.model.Categorie;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final AcademieRepository academieRepository;
    private final PasswordEncoder passwordEncoder;

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

        if (!utilisateurRepository.existsByEmail("superadmin@nadi.tn")) {
            Utilisateur superAdmin = Utilisateur.builder()
                    .email("superadmin@nadi.tn")
                    .motDePasseHash(passwordEncoder.encode("superadmin123"))
                    .role(Role.SUPER_ADMIN)
                    .actif(true)
                    .tenantId(DEFAULT_TENANT_ID)
                    .build();
            utilisateurRepository.save(superAdmin);
            log.info("Super Admin user created: superadmin@nadi.tn / superadmin123");
        }

        if (!utilisateurRepository.existsByEmail("admin@nadi.tn")) {
            Utilisateur admin = Utilisateur.builder()
                    .email("admin@nadi.tn")
                    .motDePasseHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .actif(true)
                    .tenantId(DEFAULT_TENANT_ID)
                    .build();
            utilisateurRepository.save(admin);
            log.info("Admin user created: admin@nadi.tn / admin123");
        }

        if (!utilisateurRepository.existsByEmail("coach@nadi.tn")) {
            Utilisateur coach = Utilisateur.builder()
                    .email("coach@nadi.tn")
                    .motDePasseHash(passwordEncoder.encode("coach123"))
                    .role(Role.COACH)
                    .actif(true)
                    .tenantId(DEFAULT_TENANT_ID)
                    .build();
            utilisateurRepository.save(coach);
            log.info("Coach user created: coach@nadi.tn / coach123");
        }

        if (!utilisateurRepository.existsByEmail("parent@nadi.tn")) {
            Utilisateur parent = Utilisateur.builder()
                    .email("parent@nadi.tn")
                    .motDePasseHash(passwordEncoder.encode("parent123"))
                    .role(Role.PARENT)
                    .actif(true)
                    .tenantId(DEFAULT_TENANT_ID)
                    .build();
            utilisateurRepository.save(parent);
            log.info("Parent user created: parent@nadi.tn / parent123");
        }

        if (categorieRepository.count() == 0) {
            categorieRepository.save(Categorie.builder().nom("U9").description("Formation").ageMin(6).ageMax(9).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("U13").description("Perfectionnement").ageMin(10).ageMax(13).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("U17").description("Pré-nationale").ageMin(14).ageMax(17).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("Élite").description("Sélection").ageMin(15).ageMax(19).tenantId(DEFAULT_TENANT_ID).build());
            categorieRepository.save(Categorie.builder().nom("Généraux").description("Catégorie générale — tous âges").ageMin(6).ageMax(19).tenantId(DEFAULT_TENANT_ID).build());
            log.info("5 default categories created: U9, U13, U17, Élite, Généraux");
        }

        TenantContext.clear();
    }
}
