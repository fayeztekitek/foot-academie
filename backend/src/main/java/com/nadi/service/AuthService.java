package com.nadi.service;

import com.nadi.dto.AuthResponse;
import com.nadi.dto.LoginRequest;
import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.JwtTokenProvider;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Identifiants incorrects"));

        if (Boolean.FALSE.equals(user.getActif())) {
            throw new RuntimeException("Compte désactivé");
        }

        TenantContext.setTenantId(user.getTenantId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

        String accessToken = tokenProvider.generateAccessToken(authentication, user.getTenantId());
        String refreshToken = tokenProvider.generateRefreshToken(authentication, user.getTenantId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .role(user.getRole().name())
                .email(user.getEmail())
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .tenantId(user.getTenantId())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Token de rafraîchissement invalide");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        Long tenantId = tokenProvider.getTenantIdFromToken(refreshToken);

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (Boolean.FALSE.equals(user.getActif())) {
            throw new RuntimeException("Compte désactivé");
        }

        // Rotate: generate new access AND refresh tokens
        String newAccessToken = tokenProvider.generateAccessTokenFromEmail(email, user.getTenantId());
        String newRefreshToken = tokenProvider.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(email, null), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                .role(user.getRole().name())
                .email(user.getEmail())
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .tenantId(user.getTenantId())
                .build();
    }

    public void changePassword(Utilisateur user, String ancienMotDePasse, String nouveauMotDePasse) {
        if (!passwordEncoder.matches(ancienMotDePasse, user.getMotDePasseHash())) {
            throw new RuntimeException("L'ancien mot de passe est incorrect");
        }
        user.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        user.setMustChangePassword(false);
        utilisateurRepository.save(user);
    }
}
