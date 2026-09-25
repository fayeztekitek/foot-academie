package com.nadi.security;

import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            // A refresh token must never grant API access (token-type confusion).
            // Legacy tokens without a "typ" claim are still accepted as access tokens.
            if ("refresh".equals(tokenProvider.getTokenType(token))) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = tokenProvider.getEmailFromToken(token);
            Long tenantId = tokenProvider.getTenantIdFromToken(token);

            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
            }

            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);

            if (user != null && user.getActif() && tokenVersionMatches(token, user)) {
                // Forced rotation: accounts flagged mustChangePassword may only
                // call change-password (or refresh to keep the session alive
                // until they change it). Everything else is blocked.
                if (Boolean.TRUE.equals(user.getMustChangePassword())
                        && !isPasswordChangePath(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Changement de mot de passe requis\",\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");
                    return;
                }

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (TenantContext.getTenantId() == null && user.getTenantId() != null) {
                    TenantContext.setTenantId(user.getTenantId());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean tokenVersionMatches(String token, Utilisateur user) {
        Long tokenTv = tokenProvider.getTokenVersion(token);
        if (tokenTv == null) {
            return true; // legacy pre-patch token: valid until natural expiry
        }
        long userTv = user.getTokenVersion() != null ? user.getTokenVersion() : 0L;
        return tokenTv == userTv;
    }

    private boolean isPasswordChangePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/auth/change-password") || path.endsWith("/auth/refresh");
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
