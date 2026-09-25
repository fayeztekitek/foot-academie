package com.nadi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc: @PreAuthorize is NOT enforced here; these tests cover
 * the controller's own input validation and fail-closed webhook behavior.
 */
class PaymentGatewayControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        PaymentGatewayController controller =
                new PaymentGatewayController(new com.nadi.service.PaymentGatewayService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void initiateRejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/payments/gateway/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("paiementId", 1, "amount", -50))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiateRejectsNonNumericInput() throws Exception {
        mockMvc.perform(post("/payments/gateway/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("paiementId", "abc", "amount", "xyz"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiateValidPaymentReturnsPending() throws Exception {
        mockMvc.perform(post("/payments/gateway/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("paiementId", 1, "amount", 99.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentId").exists());
    }

    @Test
    void webhookFailsClosedWithoutSecret() throws Exception {
        // WEBHOOK_SECRET is unset in test environments: the endpoint must
        // refuse everything instead of confirming arbitrary payments.
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getenv("WEBHOOK_SECRET") == null,
                "WEBHOOK_SECRET is set in this environment; fail-closed path N/A");

        mockMvc.perform(post("/payments/gateway/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("paymentId", "PAY-X", "transactionId", "TX-1"))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void checkStatusReturnsPending() throws Exception {
        mockMvc.perform(get("/payments/gateway/status/PAY-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
