package com.nadi.controller;

import com.nadi.dto.CreneauRequest;
import com.nadi.dto.CreneauResponse;
import com.nadi.service.CreneauService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
public class CreneauController {

    private final CreneauService creneauService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<List<CreneauResponse>> getAll(
            @RequestParam(required = false) String jour,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Long entraineurId) {
        if (jour != null && !jour.isBlank()) {
            return ResponseEntity.ok(creneauService.getByJour(jour));
        }
        if (categorieId != null) {
            return ResponseEntity.ok(creneauService.getByCategorie(categorieId));
        }
        if (entraineurId != null) {
            return ResponseEntity.ok(creneauService.getByEntraineur(entraineurId));
        }
        return ResponseEntity.ok(creneauService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<CreneauResponse> create(@Valid @RequestBody CreneauRequest request) {
        CreneauResponse response = creneauService.create(request);
        return ResponseEntity.created(URI.create("/slots/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<CreneauResponse> update(@PathVariable Long id, @Valid @RequestBody CreneauRequest request) {
        return ResponseEntity.ok(creneauService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        creneauService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Créneau supprimé"));
    }

    @GetMapping("/occurrences")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<List<com.nadi.dto.CreneauOccurrenceResponse>> getOccurrences(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(creneauService.getOccurrences(
                java.time.LocalDate.parse(from), java.time.LocalDate.parse(to)));
    }

    @PostMapping("/{id}/exceptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<com.nadi.dto.CreneauOccurrenceResponse> saveException(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object date = body.get("date");
        if (date == null) {
            return ResponseEntity.badRequest().build();
        }
        Object motif = body.get("motif");
        if (motif == null || motif.toString().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(creneauService.saveException(
                id,
                java.time.LocalDate.parse(date.toString()),
                body.get("statut") != null ? body.get("statut").toString() : "ANNULEE",
                body.get("heureDebut") != null ? java.time.LocalTime.parse(body.get("heureDebut").toString()) : null,
                body.get("heureFin") != null ? java.time.LocalTime.parse(body.get("heureFin").toString()) : null,
                body.get("terrain") != null ? body.get("terrain").toString() : null,
                motif.toString()));
    }

    @DeleteMapping("/{id}/exceptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Map<String, String>> deleteException(
            @PathVariable Long id, @RequestParam String date) {
        creneauService.deleteException(id, java.time.LocalDate.parse(date));
        return ResponseEntity.ok(Map.of("message", "Exception supprimée, séance restaurée"));
    }
}
