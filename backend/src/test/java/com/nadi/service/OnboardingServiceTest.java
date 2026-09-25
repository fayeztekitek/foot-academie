package com.nadi.service;

import com.nadi.dto.OnboardingRequest;
import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private AcademieRepository academieRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private OnboardingService service() {
        return new OnboardingService(academieRepository, utilisateurRepository,
                categorieRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private OnboardingRequest request(String nom, String email, String pwd) {
        return OnboardingRequest.builder()
                .academieNom(nom).academieVille("Tunis")
                .adminEmail(email).adminPrenom("A").adminNom("N")
                .adminMotDePasse(pwd).build();
    }

    @Test
    void rejectsWeakAdminPassword() {
        when(academieRepository.existsBySlug(any())).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> service().onboard(request("Nouvelle", "a@x.tn", "weak")));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateAcademyName() {
        when(academieRepository.existsBySlug(any())).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> service().onboard(request("Nadi", "a@x.tn", "StrongPass1")));
        verify(academieRepository, never()).save(any());
    }

    @Test
    void successCreatesAcademyAdminAndCategories() {
        when(academieRepository.existsBySlug(any())).thenReturn(false);
        when(academieRepository.save(any())).thenAnswer(i -> {
            Academie a = i.getArgument(0);
            a.setId(9L);
            return a;
        });
        when(passwordEncoder.encode("StrongPass1")).thenReturn("HASH");

        var response = service().onboard(request("Nouvelle", "a@x.tn", "StrongPass1"));

        assertEquals(9L, response.getAcademieId());
        assertEquals("a@x.tn", response.getAdminEmail());
        verify(utilisateurRepository).save(argThat(u ->
                u.getRole() == com.nadi.model.Role.ADMIN
                        && Long.valueOf(9L).equals(u.getTenantId())));
        verify(categorieRepository, times(5)).save(any());
    }
}
