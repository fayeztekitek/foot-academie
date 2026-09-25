package com.nadi.config;

import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private AcademieRepository academieRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Environment environment;

    private DataSeeder seeder() {
        return new DataSeeder(utilisateurRepository, categorieRepository,
                academieRepository, passwordEncoder, environment);
    }

    private Utilisateur superAdmin() {
        return Utilisateur.builder().id(1L).email("superadmin@nadi.tn")
                .motDePasseHash("h").role(Role.SUPER_ADMIN).actif(true).tenantId(1L).build();
    }

    @Test
    void prodSkipsSeedingWhenSuperAdminExists() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(utilisateurRepository.findAll()).thenReturn(List.of(superAdmin()));
        when(academieRepository.existsById(1L)).thenReturn(true);
        when(categorieRepository.count()).thenReturn(5L);

        seeder().run();

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void prodCreatesOneTimeSuperAdminWhenMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(utilisateurRepository.findAll()).thenReturn(List.of());
        when(academieRepository.existsById(1L)).thenReturn(true);
        when(categorieRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        seeder().run();

        verify(utilisateurRepository).save(argThat(u ->
                u.getRole() == Role.SUPER_ADMIN
                        && Boolean.TRUE.equals(u.getMustChangePassword())));
    }

    @Test
    void devSeedsDemoAccounts() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
        when(academieRepository.existsById(1L)).thenReturn(true);
        when(categorieRepository.count()).thenReturn(5L);

        seeder().run();

        verify(utilisateurRepository, times(4)).save(any());
    }
}
