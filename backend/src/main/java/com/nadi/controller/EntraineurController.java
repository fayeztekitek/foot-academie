package com.nadi.controller;

import com.nadi.dto.EntraineurRequest;
import com.nadi.dto.EntraineurResponse;
import com.nadi.service.EntraineurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coaches")
@RequiredArgsConstructor
public class EntraineurController {

    private final EntraineurService entraineurService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<Page<EntraineurResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        Page<EntraineurResponse> result = (search != null && !search.isBlank())
                ? entraineurService.search(search, pageable)
                : entraineurService.getAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<List<EntraineurResponse>> getAllList() {
        return ResponseEntity.ok(entraineurService.getAllList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<EntraineurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entraineurService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntraineurResponse> create(@Valid @RequestBody EntraineurRequest request) {
        EntraineurResponse response = entraineurService.create(request);
        return ResponseEntity.created(URI.create("/coaches/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntraineurResponse> update(@PathVariable Long id, @Valid @RequestBody EntraineurRequest request) {
        return ResponseEntity.ok(entraineurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        entraineurService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Entraîneur supprimé"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nouveauMotDePasse = body.get("motDePasse");
        if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe est obligatoire"));
        }
        if (nouveauMotDePasse.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe doit contenir au moins 8 caractères"));
        }
        entraineurService.resetPassword(id, nouveauMotDePasse);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}
