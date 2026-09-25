package com.nadi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(Authentication authentication, Long tenantId, long tokenVersion) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), accessTokenExpirationMs, tenantId, "access", tokenVersion);
    }

    public String generateRefreshToken(Authentication authentication, Long tenantId, long tokenVersion) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername(), refreshTokenExpirationMs, tenantId, "refresh", tokenVersion);
    }

    public String generateAccessTokenFromEmail(String email, Long tenantId, long tokenVersion) {
        return generateToken(email, accessTokenExpirationMs, tenantId, "access", tokenVersion);
    }

    private String generateToken(String subject, long expirationMs, Long tenantId, String type, long tokenVersion) {
        return Jwts.builder()
                .subject(subject)
                .claim("tenantId", tenantId)
                .claim("typ", type)
                .claim("tv", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long getTenantIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object tenantId = claims.get("tenantId");
        if (tenantId instanceof Number) {
            return ((Number) tenantId).longValue();
        }
        return null;
    }

    /** Token use: "access", "refresh", or null for legacy pre-patch tokens. */
    public String getTokenType(String token) {
        return getClaims(token).get("typ", String.class);
    }

    /** Token version for revocation; null for legacy pre-patch tokens. */
    public Long getTokenVersion(String token) {
        Object tv = getClaims(token).get("tv");
        if (tv instanceof Number) {
            return ((Number) tv).longValue();
        }
        return null;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}
