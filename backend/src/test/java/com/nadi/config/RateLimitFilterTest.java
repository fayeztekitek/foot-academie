package com.nadi.config;

import com.nadi.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimitFilter();
        TenantContext.clear();
        StringWriter writer = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(writer));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private void loginAttempt() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        filter.doFilter(request, response, chain);
    }

    @Test
    void sixthRapidLoginIsRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            loginAttempt();
        }
        verify(chain, times(5)).doFilter(request, response);

        loginAttempt();

        verify(response).setStatus(429);
        verify(chain, times(5)).doFilter(request, response);
    }

    @Test
    void unrelatedPathsAreNotThrottled() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/players");
        TenantContext.setTenantId(1L);

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void protectedPathWithoutTenantIsForbidden() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/players");

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void publicAuthPathPassesWithoutTenant() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.4");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void refreshStormIsThrottled() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/refresh");
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        for (int i = 0; i < 11; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, times(10)).doFilter(request, response);
        verify(response).setStatus(429);
    }
}
