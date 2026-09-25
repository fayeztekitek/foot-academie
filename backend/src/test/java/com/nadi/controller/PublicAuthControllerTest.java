package com.nadi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc: method-security (@PreAuthorize) is NOT enforced here,
 * so these tests cover the controller's own validation and scoping logic.
 */
@ExtendWith(MockitoExtension.class)
class PublicAuthControllerTest {

    @Mock
    private AcademieRepository academieRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PublicAuthController controller = new PublicAuthController(
                academieRepository, utilisateurRepository, passwordEncoder);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    }

    private String body(Object email, Object password, Object tenantId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "email", email == null ? "" : email,
                "newPassword", password == null ? "" : password,
                "tenantId", tenantId == null ? "" : tenantId));
    }

    @Test
    void resetRequiresTenantScope() throws Exception {
        mockMvc.perform(post("/auth/admin/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "v@x.tn", "newPassword", "StrongPass1"))))
                .andExpect(status().isBadRequest());
        verify(utilisateurRepository, never()).updatePasswordByEmail(anyString(), anyString());
    }

    @Test
    void resetRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/auth/admin/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("v@x.tn", "short", 5)))
                .andExpect(status().isBadRequest());
        verify(utilisateurRepository, never())
                .updatePasswordByEmailAndTenantId(anyString(), anyString(), any());
    }

    @Test
    void resetRejectsMalformedTenantId() throws Exception {
        mockMvc.perform(post("/auth/admin/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("v@x.tn", "StrongPass1", "not-a-number")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetUnknownUserReturns404() throws Exception {
        when(utilisateurRepository.updatePasswordByEmailAndTenantId("ghost@x.tn", "HASH", 5L))
                .thenReturn(0);

        mockMvc.perform(post("/auth/admin/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ghost@x.tn", "StrongPass1", 5)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetScopedToTenantSucceeds() throws Exception {
        when(utilisateurRepository.updatePasswordByEmailAndTenantId("v@x.tn", "HASH", 5L))
                .thenReturn(1);

        mockMvc.perform(post("/auth/admin/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("v@x.tn", "StrongPass1", 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
        verify(utilisateurRepository).updatePasswordByEmailAndTenantId("v@x.tn", "HASH", 5L);
    }
}
