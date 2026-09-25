package com.nadi.service;

import com.nadi.dto.AuthResponse;
import com.nadi.dto.LoginRequest;
import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.JwtTokenProvider;
import com.nadi.security.PasswordPolicy;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
        TenantContext.setTenantId(request.getTenantId());

        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Identifiants incorrects"));

        if (Boolean.FALSE.equals(user.getActif())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        if (!user.getTenantId().equals(request.getTenantId())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

        long tokenVersion = currentTokenVersion(user);
        String accessToken = tokenProvider.generateAccessToken(authentication, user.getTenantId(), tokenVersion);
        String refreshToken = tokenProvider.generateRefreshToken(authentication, user.getTenantId(), tokenVersion);

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

        // Token-type confusion guard: only refresh-typed tokens accepted here.
        // Legacy tokens without a "typ" claim are still honored until expiry.
        String presentedType = tokenProvider.getTokenType(refreshToken);
        if (presentedType != null && !"refresh".equals(presentedType)) {
            throw new RuntimeException("Token de rafraîchissement invalide");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (Boolean.FALSE.equals(user.getActif())) {
            throw new RuntimeException("Compte désactivé");
        }

        Long presentedTv = tokenProvider.getTokenVersion(refreshToken);
        if (presentedTv != null && presentedTv != currentTokenVersion(user)) {
            throw new RuntimeException("Token de rafraîchissement révoqué");
        }

        // Rotate: generate new access AND refresh tokens
        long tokenVersion = currentTokenVersion(user);
        String newAccessToken = tokenProvider.generateAccessTokenFromEmail(email, user.getTenantId(), tokenVersion);
        String newRefreshToken = tokenProvider.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(email, null), user.getTenantId(), tokenVersion);

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
        PasswordPolicy.validateOrThrow(nouveauMotDePasse);
        user.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        user.setMustChangePassword(false);
        // Revoke all previously issued tokens for this account.
        user.setTokenVersion(currentTokenVersion(user) + 1);
        utilisateurRepository.save(user);
    }

    private long currentTokenVersion(Utilisateur user) {
        return user.getTokenVersion() != null ? user.getTokenVersion() : 0L;
    }
}
