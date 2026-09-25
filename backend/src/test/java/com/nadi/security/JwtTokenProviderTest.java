package com.nadi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha-256!!";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, 3_600_000L, 604_800_000L);
    }

    private UsernamePasswordAuthenticationToken authOf(String email) {
        return new UsernamePasswordAuthenticationToken(email, null);
    }

    @Test
    void accessTokenCarriesAccessTypeTenantAndVersion() {
        String token = provider.generateAccessToken(authOf("a@nadi.tn"), 7L, 3L);

        assertTrue(provider.validateToken(token));
        assertEquals("access", provider.getTokenType(token));
        assertEquals(7L, provider.getTenantIdFromToken(token));
        assertEquals(3L, provider.getTokenVersion(token));
        assertEquals("a@nadi.tn", provider.getEmailFromToken(token));
    }

    @Test
    void refreshTokenCarriesRefreshType() {
        String token = provider.generateRefreshToken(authOf("a@nadi.tn"), 7L, 0L);

        assertTrue(provider.validateToken(token));
        assertEquals("refresh", provider.getTokenType(token));
    }

    @Test
    void accessAndRefreshTokensAreStructurallyDistinct() {
        String access = provider.generateAccessToken(authOf("a@nadi.tn"), 7L, 0L);
        String refresh = provider.generateRefreshToken(authOf("a@nadi.tn"), 7L, 0L);

        assertNotEquals(access, refresh);
        assertNotEquals(provider.getTokenType(access), provider.getTokenType(refresh));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = provider.generateAccessToken(authOf("a@nadi.tn"), 7L, 0L);

        assertFalse(provider.validateToken(token + "tampered"));
    }

    @Test
    void expiredTokenIsRejected() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(SECRET.getBytes())));
        String expired = Jwts.builder()
                .subject("a@nadi.tn")
                .claim("tenantId", 7L)
                .issuedAt(new Date(System.currentTimeMillis() - 20_000))
                .expiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(key)
                .compact();

        assertFalse(provider.validateToken(expired));
    }

    @Test
    void legacyTokenWithoutClaimsYieldsNullTypeAndVersion() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(SECRET.getBytes())));
        String legacy = Jwts.builder()
                .subject("a@nadi.tn")
                .claim("tenantId", 7L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertTrue(provider.validateToken(legacy));
        assertNull(provider.getTokenType(legacy));
        assertNull(provider.getTokenVersion(legacy));
    }
}
