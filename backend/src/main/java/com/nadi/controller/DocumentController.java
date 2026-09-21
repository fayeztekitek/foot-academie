package com.nadi.controller;

import com.nadi.dto.DocumentRequest;
import com.nadi.dto.DocumentResponse;
import com.nadi.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/joueur/{joueurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<List<DocumentResponse>> getByJoueur(@PathVariable Long joueurId) {
        return ResponseEntity.ok(documentService.getByJoueur(joueurId));
    }

    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<List<DocumentResponse>> getByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(documentService.getByParent(parentId));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getExpiring(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(documentService.getExpiringWithinDays(days));
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getExpired() {
        return ResponseEntity.ok(documentService.getExpired());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentService.create(request));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam Long joueurId,
            @RequestParam String type,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(documentService.upload(joueurId, type, file));
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statutStr = body.get("statut");
        com.nadi.model.StatutDocument statut;
        try {
            statut = com.nadi.model.StatutDocument.valueOf(statutStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(documentService.updateStatut(id, statut));
    }
}
