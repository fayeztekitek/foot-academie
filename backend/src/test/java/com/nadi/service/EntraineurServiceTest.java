package com.nadi.service;

import com.nadi.dto.EntraineurRequest;
import com.nadi.model.Entraineur;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.CategorieRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntraineurServiceTest {

    @Mock
    private EntraineurRepository entraineurRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private EntraineurService service() {
        return new EntraineurService(entraineurRepository, categorieRepository,
                utilisateurRepository, passwordEncoder);
    }

    private EntraineurRequest request(String email, String pwd) {
        EntraineurRequest request = new EntraineurRequest();
        request.setPrenom("C");
        request.setNom("N");
        request.setEmail(email);
        request.setMotDePasse(pwd);
        return request;
    }

    @Test
    void createRejectsWeakCustomPassword() {
        assertThrows(RuntimeException.class,
                () -> service().create(request("c@nadi.tn", "weak")));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void createGeneratesStrongTemporaryPassword() {
        when(entraineurRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(utilisateurRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        service().create(request("c@nadi.tn", null));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(captor.capture());
        assertEquals(12, captor.getValue().length());
        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());
        assertEquals(Role.COACH, userCaptor.getValue().getRole());
        assertTrue(userCaptor.getValue().getMustChangePassword());
    }

    @Test
    void resetPasswordRevokesSessions() {
        Utilisateur user = Utilisateur.builder()
                .id(8L).email("c@nadi.tn").motDePasseHash("old")
                .role(Role.COACH).actif(true)
                .mustChangePassword(false).tokenVersion(0L).tenantId(1L).build();
        Entraineur coach = Entraineur.builder().id(5L).prenom("C").nom("N").email("c@nadi.tn").build();
        coach.setUtilisateur(user);
        when(entraineurRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(passwordEncoder.encode("NewStrong1")).thenReturn("HASH");

        service().resetPassword(5L, "NewStrong1");

        assertEquals("HASH", user.getMotDePasseHash());
        assertEquals(1L, user.getTokenVersion());
    }
}
