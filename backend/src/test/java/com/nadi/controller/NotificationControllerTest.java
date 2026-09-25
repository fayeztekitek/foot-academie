package com.nadi.controller;

import com.nadi.model.DeviceToken;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.DeviceTokenRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc: @PreAuthorize is NOT enforced here; these tests cover
 * the ownership scoping of device unregistration.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(
                notificationService, utilisateurRepository, deviceTokenRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Utilisateur user = Utilisateur.builder()
                .id(42L).email("u@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_PARENT"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private DeviceToken tokenOf(Long ownerId) {
        Utilisateur owner = Utilisateur.builder()
                .id(ownerId).email("o@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
        return DeviceToken.builder().id(1L).token("fcm-token").utilisateur(owner)
                .platform("android").tenantId(1L).build();
    }

    @Test
    void ownerCanUnregisterOwnDevice() throws Exception {
        when(deviceTokenRepository.findByToken("fcm-token"))
                .thenReturn(Optional.of(tokenOf(42L)));

        mockMvc.perform(delete("/notifications/unregister-device").param("token", "fcm-token"))
                .andExpect(status().isOk());
        verify(deviceTokenRepository).delete(any());
    }

    @Test
    void strangerCannotUnregisterOthersDevice() throws Exception {
        when(deviceTokenRepository.findByToken("fcm-token"))
                .thenReturn(Optional.of(tokenOf(99L)));

        mockMvc.perform(delete("/notifications/unregister-device").param("token", "fcm-token"))
                .andExpect(status().isOk());
        verify(deviceTokenRepository, never()).delete(any());
    }

    @Test
    void unknownTokenIsNoop() throws Exception {
        when(deviceTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/notifications/unregister-device").param("token", "nope"))
                .andExpect(status().isOk());
        verify(deviceTokenRepository, never()).delete(any());
    }
}
