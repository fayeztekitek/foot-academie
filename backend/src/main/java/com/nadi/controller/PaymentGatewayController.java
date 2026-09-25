package com.nadi.controller;

import com.nadi.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments/gateway")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentGatewayController {

    private final PaymentGatewayService gatewayService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody Map<String, Object> body) {
        long paiementId;
        double amount;
        try {
            paiementId = Long.parseLong(String.valueOf(body.get("paiementId")));
            amount = Double.parseDouble(String.valueOf(body.get("amount")));
        } catch (NumberFormatException | NullPointerException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "paiementId et amount numériques requis"));
        }
        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le montant doit être positif"));
        }
        String currency = (String) body.getOrDefault("currency", "TND");
        String description = (String) body.getOrDefault("description", "Paiement Nadi");

        return ResponseEntity.ok(gatewayService.initiatePayment(paiementId, amount, currency, description));
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Map<String, String>> checkStatus(@PathVariable String paymentId) {
        return ResponseEntity.ok(gatewayService.checkPaymentStatus(paymentId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> webhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
            @RequestBody Map<String, String> body) {
        String configuredSecret = System.getenv("WEBHOOK_SECRET");
        // Fail closed: without a configured secret this endpoint must refuse
        // everything instead of confirming arbitrary payments.
        if (configuredSecret == null || configuredSecret.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Passerelle de paiement non configurée"));
        }
        if (webhookSecret == null || !constantTimeEquals(configuredSecret, webhookSecret)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }
        String paymentId = body.get("paymentId");
        String transactionId = body.get("transactionId");
        if (paymentId == null || paymentId.isBlank() || transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "paymentId et transactionId requis"));
        }
        boolean confirmed = gatewayService.confirmPayment(paymentId, transactionId);
        return ResponseEntity.ok(Map.of(
                "status", confirmed ? "confirmed" : "failed",
                "paymentId", paymentId
        ));
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] e = expected.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] a = actual.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(e, a);
    }
}
