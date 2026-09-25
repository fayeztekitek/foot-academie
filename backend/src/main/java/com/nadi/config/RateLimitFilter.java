package com.nadi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.nadi.tenant.TenantContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
// Explicit order: this filter must run AFTER the Spring Security chain (where
// JwtAuthenticationFilter sets the TenantContext). Relying on default ordering
// made the tenant gate below order-fragile (valid JWT requests could 403).
@Order(Ordered.LOWEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, RequestCounter> counterMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Whitelist public endpoints — no tenant needed
        boolean isPublic = path.startsWith("/api/auth/")
                || path.startsWith("/onboarding/")
                || path.equals("/api/health")
                || path.equals("/actuator/health");

        if (!isPublic && TenantContext.getTenantId() == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Tenant context not set\"}");
            return;
        }

        // Per-endpoint throttles (keyed by client IP + path).
        int limit = limitFor(path);
        if (limit > 0) {
            String key = getClientIp(request) + ":" + path;
            RequestCounter counter = counterMap.computeIfAbsent(key, k -> new RequestCounter());
            if (!counter.allowRequest(limit)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Trop de tentatives. Réessayez dans 1 minute.\"}");
                return;
            }
        }

        counterMap.entrySet().removeIf(e -> e.getValue().isExpired());
        filterChain.doFilter(request, response);
    }

    private int limitFor(String path) {
        if (path.contains("/auth/login")) return 5;
        if (path.contains("/auth/refresh")) return 10;
        if (path.contains("/invitations/accept")) return 10;
        if (path.contains("/ai/chat")) return 30;
        return 0;
    }

    private String getClientIp(HttpServletRequest request) {
        // Use remoteAddr only — X-Forwarded-For can be spoofed by clients
        return request.getRemoteAddr();
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean allowRequest(int max) {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= max;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > 120_000;
        }
    }
}
