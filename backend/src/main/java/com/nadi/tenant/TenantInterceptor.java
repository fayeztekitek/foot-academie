package com.nadi.tenant;

import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    private final AcademieRepository academieRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        if (path.startsWith("/actuator") || path.startsWith("/h2-console")) {
            return true;
        }

        if (path.contains("/invitations/accept") || path.contains("/invitations/by-token/")) {
            return true;
        }

        if (path.contains("/onboarding")) {
            return true;
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return true;
        }

        log.debug("No tenant context for request: {} (may be set by JWT filter)", path);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Tenant context not set\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
