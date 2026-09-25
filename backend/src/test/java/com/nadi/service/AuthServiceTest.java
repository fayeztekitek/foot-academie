package com.nadi.service;

import com.nadi.dto.AuthResponse;
import com.nadi.dto.LoginRequest;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.JwtTokenProvider;
import com.nadi.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha-256!!";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 3_600_000L, 604_800_000L);
        authService = new AuthService(authenticationManager, tokenProvider, utilisateurRepository, passwordEncoder);
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Utilisateur user(Long tenantId, boolean actif) {
        return Utilisateur.builder()
                .id(1L).email("admin@nadi.tn").motDePasseHash("hash")
                .role(Role.ADMIN).actif(actif).tenantId(tenantId)
                .mustChangePassword(false).tokenVersion(0L)
                .build();
    }

    private LoginRequest login(String email, String pwd, Long tenantId) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setMotDePasse(pwd);
        request.setTenantId(tenantId);
        return request;
    }

    @Test
    void loginSuccessReturnsTypedTokens() {
        when(utilisateurRepository.findByEmail("admin@nadi.tn"))
                .thenReturn(Optional.of(user(1L, true)));
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin@nadi.tn", null));

        AuthResponse response = authService.login(login("admin@nadi.tn", "admin123", 1L));

        assertEquals("ADMIN", response.getRole());
        assertEquals(1L, response.getTenantId());
        assertEquals("access", tokenProvider.getTokenType(response.getAccessToken()));
        assertEquals("refresh", tokenProvider.getTokenType(response.getRefreshToken()));
    }

    @Test
    void loginRejectsWrongTenant() {
        when(utilisateurRepository.findByEmail("admin@nadi.tn"))
                .thenReturn(Optional.of(user(1L, true)));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(login("admin@nadi.tn", "admin123", 2L)));
    }

    @Test
    void loginRejectsInactiveAccount() {
        when(utilisateurRepository.findByEmail("admin@nadi.tn"))
                .thenReturn(Optional.of(user(1L, false)));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(login("admin@nadi.tn", "admin123", 1L)));
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(utilisateurRepository.findByEmail("ghost@nadi.tn"))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(login("ghost@nadi.tn", "whatever12", 1L)));
    }

    @Test
    void refreshRejectsAccessToken() {
        String access = tokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken("admin@nadi.tn", null), 1L, 0L);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(access));
    }

    @Test
    void refreshRotatesTokenPair() {
        when(utilisateurRepository.findByEmail("admin@nadi.tn"))
                .thenReturn(Optional.of(user(1L, true)));
        String refresh = tokenProvider.generateRefreshToken(
                new UsernamePasswordAuthenticationToken("admin@nadi.tn", null), 1L, 0L);

        AuthResponse response = authService.refreshToken(refresh);

        assertEquals("ADMIN", response.getRole());
        assertEquals("access", tokenProvider.getTokenType(response.getAccessToken()));
        assertEquals("refresh", tokenProvider.getTokenType(response.getRefreshToken()));
    }

    @Test
    void refreshRejectsRevokedVersion() {
        Utilisateur rotated = user(1L, true);
        rotated.setTokenVersion(2L);
        when(utilisateurRepository.findByEmail("admin@nadi.tn"))
                .thenReturn(Optional.of(rotated));
        String staleRefresh = tokenProvider.generateRefreshToken(
                new UsernamePasswordAuthenticationToken("admin@nadi.tn", null), 1L, 0L);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(staleRefresh));
    }

    @Test
    void changePasswordRevokesOldTokens() {
        Utilisateur u = user(1L, true);
        when(passwordEncoder.matches("oldPassword1", "hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword1")).thenReturn("newhash");

        authService.changePassword(u, "oldPassword1", "newPassword1");

        assertEquals("newhash", u.getMotDePasseHash());
        assertFalse(u.getMustChangePassword());
        assertEquals(1L, u.getTokenVersion());
        verify(utilisateurRepository).save(u);
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        Utilisateur u = user(1L, true);
        when(passwordEncoder.matches("badoldpass", "hash")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.changePassword(u, "badoldpass", "newPassword1"));
        verify(utilisateurRepository, never()).save(any());
    }
}
