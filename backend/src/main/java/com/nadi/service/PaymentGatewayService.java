package com.nadi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayService {

    public static class PaymentInitResponse {
        public final String paymentId;
        public final String paymentUrl;
        public final String status;

        public PaymentInitResponse(String paymentId, String paymentUrl, String status) {
            this.paymentId = paymentId;
            this.paymentUrl = paymentUrl;
            this.status = status;
        }
    }

    public PaymentInitResponse initiatePayment(Long paiementId, double amount, String currency, String description) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // TODO: Replace with real Paymee/ClicToPay integration
        String paymentUrl = "https://sandbox.paymee.tn/payment/" + paymentId;

        log.info("Payment initiated: {} | Amount: {} {} | Description: {}", paymentId, amount, currency, description);

        return new PaymentInitResponse(paymentId, paymentUrl, "PENDING");
    }

    public Map<String, String> checkPaymentStatus(String paymentId) {
        // TODO: Replace with real API call
        log.info("Checking payment status for: {}", paymentId);
        return Map.of(
                "paymentId", paymentId,
                "status", "PENDING",
                "message", "Statut à vérifier via la passerelle de paiement"
        );
    }

    public boolean confirmPayment(String paymentId, String transactionId) {
        // TODO: Verify with gateway webhook
        log.info("Confirming payment: {} | Transaction: {}", paymentId, transactionId);
        return true;
    }
}
