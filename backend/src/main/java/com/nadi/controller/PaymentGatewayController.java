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
    public ResponseEntity<PaymentGatewayService.PaymentInitResponse> initiate(@RequestBody Map<String, Object> body) {
        Long paiementId = Long.valueOf(body.get("paiementId").toString());
        double amount = Double.parseDouble(body.get("amount").toString());
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
        if (configuredSecret != null && !configuredSecret.isEmpty()) {
            if (webhookSecret == null || !configuredSecret.equals(webhookSecret)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Unauthorized"));
            }
        }
        String paymentId = body.get("paymentId");
        String transactionId = body.get("transactionId");
        boolean confirmed = gatewayService.confirmPayment(paymentId, transactionId);
        return ResponseEntity.ok(Map.of(
                "status", confirmed ? "confirmed" : "failed",
                "paymentId", paymentId
        ));
    }
}
