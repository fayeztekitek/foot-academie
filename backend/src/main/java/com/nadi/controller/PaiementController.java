package com.nadi.controller;

import com.nadi.dto.PaiementRequest;
import com.nadi.dto.PaiementResponse;
import com.nadi.model.Parent;
import com.nadi.repository.ParentRepository;
import com.nadi.security.SecurityUtils;
import com.nadi.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final SecurityUtils securityUtils;
    private final ParentRepository parentRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<Page<PaiementResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long joueurId,
            @RequestParam(required = false) Long parentId) {
        PageRequest pageable = PageRequest.of(page, size);

        if (securityUtils.isParent()) {
            Long myParentId = parentRepository.findByUtilisateurId(securityUtils.getCurrentUserId())
                    .map(Parent::getId)
                    .orElse(null);
            if (myParentId != null) {
                return ResponseEntity.ok(paiementService.getByParent(myParentId, pageable));
            }
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Page<PaiementResponse> result;
        if (statut != null && !statut.isBlank()) {
            result = paiementService.getByStatut(statut, pageable);
        } else if (joueurId != null) {
            result = paiementService.getByJoueur(joueurId, pageable);
        } else if (parentId != null) {
            result = paiementService.getByParent(parentId, pageable);
        } else {
            result = paiementService.getAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementResponse>> getOverdue() {
        return ResponseEntity.ok(paiementService.getOverdue());
    }

    @GetMapping("/month/{year}/{month}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaiementResponse>> getByMonth(
            @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(paiementService.getByMonth(year, month));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementResponse> create(@Valid @RequestBody PaiementRequest request) {
        PaiementResponse response = paiementService.create(request);
        return ResponseEntity.created(URI.create("/payments/" + response.getId())).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementResponse> update(@PathVariable Long id, @RequestBody PaiementRequest request) {
        return ResponseEntity.ok(paiementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        paiementService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Paiement supprimé"));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementResponse> markAsPaid(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        String moyen = body != null ? (String) body.get("moyenPaiement") : null;
        String numeroRecu = body != null ? (String) body.get("numeroRecu") : null;
        String commentaire = body != null ? (String) body.get("commentaire") : null;
        java.math.BigDecimal montant = null;
        if (body != null && body.get("montant") != null) {
            montant = new java.math.BigDecimal(body.get("montant").toString());
        }
        java.time.LocalDate datePaiement = null;
        if (body != null && body.get("datePaiement") != null) {
            datePaiement = java.time.LocalDate.parse((String) body.get("datePaiement"));
        }
        return ResponseEntity.ok(paiementService.markAsPaid(id, moyen, numeroRecu, commentaire, montant, datePaiement));
    }

    @PostMapping("/generate-schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateSchedule(@RequestBody Map<String, Object> body) {
        Long joueurId = Long.valueOf(body.get("joueurId").toString());
        Long parentId = Long.valueOf(body.get("parentId").toString());
        String formule = (String) body.get("formule");
        String startDateStr = (String) body.get("startDate");

        paiementService.generateSchedule(
                joueurId,
                parentId,
                com.nadi.model.FormulePaiement.valueOf(formule),
                java.time.LocalDate.parse(startDateStr)
        );
        return ResponseEntity.ok(Map.of("message", "Échéancier généré avec succès"));
    }
}
