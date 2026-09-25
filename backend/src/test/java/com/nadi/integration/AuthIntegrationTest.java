package com.nadi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadi.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final AtomicInteger IP_SEQ = new AtomicInteger(10);

    /** Unique client IP per login so the login rate limiter never trips in tests. */
    private static RequestPostProcessor freshIp() {
        String ip = "10.20.30." + IP_SEQ.incrementAndGet();
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@nadi.tn");
        login.setMotDePasse("admin123");
        login.setTenantId(1L);

        mockMvc.perform(post("/api/auth/login")
                        .contextPath("/api")
                        .with(freshIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("admin@nadi.tn");
        login.setMotDePasse("wrongpassword");
        login.setTenantId(1L);

        mockMvc.perform(post("/api/auth/login")
                        .contextPath("/api")
                        .with(freshIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpointWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/players").contextPath("/api"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthEndpointIsAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
