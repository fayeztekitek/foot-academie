package com.nadi.controller;

import com.nadi.dto.JoueurRequest;
import com.nadi.dto.JoueurResponse;
import com.nadi.model.Parent;
import com.nadi.repository.ParentRepository;
import com.nadi.security.SecurityUtils;
import com.nadi.service.JoueurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class JoueurController {

    private final JoueurService joueurService;
    private final SecurityUtils securityUtils;
    private final ParentRepository parentRepository;

    @GetMapping
    public ResponseEntity<Page<JoueurResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Long parentId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nom").ascending());

        if (securityUtils.isParent()) {
            Long myParentId = parentRepository.findByUtilisateurId(securityUtils.getCurrentUserId())
                    .map(Parent::getId)
                    .orElse(null);
            if (myParentId != null) {
                return ResponseEntity.ok(joueurService.getByParent(myParentId, pageable));
            }
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Page<JoueurResponse> result;
        if (categorieId != null) {
            result = joueurService.getByCategorie(categorieId, pageable);
        } else if (parentId != null) {
            result = joueurService.getByParent(parentId, pageable);
        } else if (search != null && !search.isBlank()) {
            result = joueurService.search(search, pageable);
        } else {
            result = joueurService.getAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JoueurResponse> getById(@PathVariable Long id) {
        JoueurResponse joueur = joueurService.getById(id);

        if (securityUtils.isParent()) {
            Long myParentId = parentRepository.findByUtilisateurId(securityUtils.getCurrentUserId())
                    .map(Parent::getId)
                    .orElse(null);
            if (myParentId == null || !myParentId.equals(joueur.getParentId())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(joueur);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<JoueurResponse> create(@Valid @RequestBody JoueurRequest request) {
        JoueurResponse response = joueurService.create(request);
        return ResponseEntity.created(URI.create("/players/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<JoueurResponse> update(@PathVariable Long id, @Valid @RequestBody JoueurRequest request) {
        return ResponseEntity.ok(joueurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        joueurService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Joueur supprimé"));
    }

    @PatchMapping("/{id}/frequence-paiement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JoueurResponse> changeFrequence(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String frequence = body.get("frequence");
        return ResponseEntity.ok(joueurService.changeFrequence(id, frequence));
    }
}
