package com.nadi.controller;

import com.nadi.dto.AbonnementRequest;
import com.nadi.dto.AbonnementResponse;
import com.nadi.dto.FactureResponse;
import com.nadi.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/abonnement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonnementResponse> getCurrentAbonnement() {
        return ResponseEntity.ok(billingService.getCurrentAbonnement());
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonnementResponse> subscribe(@Valid @RequestBody AbonnementRequest request) {
        return ResponseEntity.ok(billingService.subscribe(request));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonnementResponse> cancel() {
        return ResponseEntity.ok(billingService.cancelAbonnement());
    }

    @GetMapping("/factures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FactureResponse>> getFactures() {
        return ResponseEntity.ok(billingService.getFactures());
    }

    @GetMapping("/plans")
    public ResponseEntity<Map<String, Object>> getPlans() {
        return ResponseEntity.ok(Map.of(
            "plans", Map.of(
                "FREE", Map.of("label", "Gratuit", "maxJoueurs", 10, "maxCoachs", 2, "maxParents", 20, "prix", "0 TND/mois"),
                "PRO", Map.of("label", "Professionnel", "maxJoueurs", 100, "maxCoachs", 10, "maxParents", 500, "prix", "99 TND/mois"),
                "PREMIUM", Map.of("label", "Premium", "maxJoueurs", 999, "maxCoachs", 99, "maxParents", 9999, "prix", "249 TND/mois")
            )
        ));
    }
}
