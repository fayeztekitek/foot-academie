package com.nadi.service;

import com.nadi.dto.ParentRequest;
import com.nadi.model.Parent;
import com.nadi.model.Utilisateur;
import com.nadi.repository.ParentRepository;
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
class ParentServiceTest {

    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    private ParentService service() {
        return new ParentService(parentRepository, utilisateurRepository, passwordEncoder, emailService);
    }

    private ParentRequest request(String email, String pwd) {
        ParentRequest request = new ParentRequest();
        request.setPrenom("P");
        request.setNom("N");
        request.setEmail(email);
        request.setMotDePasse(pwd);
        return request;
    }

    @Test
    void createRejectsWeakCustomPassword() {
        assertThrows(RuntimeException.class,
                () -> service().create(request("p@nadi.tn", "weak")));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void createGeneratesStrongTemporaryPassword() {
        when(parentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(utilisateurRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        service().create(request("p@nadi.tn", null));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(captor.capture());
        assertEquals(12, captor.getValue().length());
        verify(emailService).sendParentCredentials(any(), any(), any(), any(), any());
    }

    @Test
    void resetPasswordRevokesSessions() {
        Utilisateur user = Utilisateur.builder()
                .id(7L).email("p@nadi.tn").motDePasseHash("old")
                .role(com.nadi.model.Role.PARENT).actif(true)
                .mustChangePassword(false).tokenVersion(0L).tenantId(1L).build();
        Parent parent = Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build();
        parent.setUtilisateur(user);
        when(parentRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(passwordEncoder.encode("NewStrong1")).thenReturn("HASH");

        service().resetPassword(10L, "NewStrong1");

        assertEquals("HASH", user.getMotDePasseHash());
        assertTrue(user.getMustChangePassword());
        assertEquals(1L, user.getTokenVersion());
    }
}
