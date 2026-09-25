package com.nadi.service;

import com.nadi.dto.AcademieRequest;
import com.nadi.dto.AcademieResponse;
import com.nadi.model.Academie;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.*;
import com.nadi.security.SecurityUtils;
import com.nadi.tenant.TenantContext;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademieServiceTest {

    @Mock
    private AcademieRepository academieRepository;
    @Mock
    private AbonnementRepository abonnementRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AcademieService academieService;

    @BeforeEach
    void setUp() {
        academieService = new AcademieService(
                academieRepository, abonnementRepository, joueurRepository, parentRepository,
                entraineurRepository, utilisateurRepository, categorieRepository,
                passwordEncoder, new SecurityUtils(utilisateurRepository));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Role role, Long tenantId) {
        Utilisateur user = Utilisateur.builder()
                .id(1L).email("u@nadi.tn").motDePasseHash("hash")
                .role(role).actif(true).tenantId(tenantId).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
        TenantContext.setTenantId(tenantId);
    }

    private Academie academy(Long id) {
        return Academie.builder().id(id).slug("slug-" + id).nom("Acad " + id)
                .ville("Tunis").active(true).plan("PRO").build();
    }

    @Test
    void adminCanReadOwnAcademy() {
        authenticateAs(Role.ADMIN, 5L);
        when(academieRepository.findById(5L)).thenReturn(Optional.of(academy(5L)));

        AcademieResponse response = academieService.getById(5L);

        assertEquals(5L, response.getId());
    }

    @Test
    void adminCannotReadOtherAcademy() {
        authenticateAs(Role.ADMIN, 5L);

        assertThrows(AccessDeniedException.class, () -> academieService.getById(6L));
        verify(academieRepository, never()).findById(any());
    }

    @Test
    void superAdminCanReadAnyAcademy() {
        authenticateAs(Role.SUPER_ADMIN, 1L);
        when(academieRepository.findById(6L)).thenReturn(Optional.of(academy(6L)));

        assertEquals(6L, academieService.getById(6L).getId());
    }

    @Test
    void adminCannotUpdateOtherAcademy() {
        authenticateAs(Role.ADMIN, 5L);

        assertThrows(AccessDeniedException.class,
                () -> academieService.update(6L, AcademieRequest.builder().nom("X").build()));
        verify(academieRepository, never()).save(any());
    }

    @Test
    void adminCannotToggleOtherAcademy() {
        authenticateAs(Role.ADMIN, 5L);

        assertThrows(AccessDeniedException.class, () -> academieService.toggleActive(6L));
        verify(academieRepository, never()).save(any());
    }

    @Test
    void createWithAdminCredentialsCreatesUserAndCategories() {
        when(academieRepository.existsBySlug("nouvelle")).thenReturn(false);
        when(academieRepository.save(any())).thenAnswer(i -> {
            Academie a = i.getArgument(0);
            a.setId(9L);
            return a;
        });
        when(passwordEncoder.encode("StrongPass1")).thenReturn("hash");

        AcademieRequest request = AcademieRequest.builder()
                .slug("nouvelle").nom("Nouvelle").adminEmail("admin@nouvelle.tn")
                .adminMotDePasse("StrongPass1").plan("FREE").build();

        AcademieResponse response = academieService.create(request);

        assertEquals(9L, response.getId());
        verify(utilisateurRepository).save(any());
        verify(categorieRepository, times(5)).save(any());
    }

    @Test
    void createRejectsWeakAdminPassword() {
        when(academieRepository.existsBySlug("nouvelle")).thenReturn(false);

        AcademieRequest request = AcademieRequest.builder()
                .slug("nouvelle").nom("Nouvelle").adminEmail("admin@nouvelle.tn")
                .adminMotDePasse("weak").plan("FREE").build();

        assertThrows(RuntimeException.class, () -> academieService.create(request));
        verify(utilisateurRepository, never()).save(any());
    }
}
