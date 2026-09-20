package com.nadi.controller;

import com.nadi.dto.NoteJoueurRequest;
import com.nadi.dto.NoteJoueurResponse;
import com.nadi.service.NoteJoueurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteJoueurController {

    private final NoteJoueurService noteJoueurService;

    @GetMapping("/joueur/{joueurId}")
    public ResponseEntity<Page<NoteJoueurResponse>> getByJoueur(
            @PathVariable Long joueurId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(noteJoueurService.getByJoueur(joueurId, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<NoteJoueurResponse> create(@Valid @RequestBody NoteJoueurRequest request) {
        return ResponseEntity.ok(noteJoueurService.create(request));
    }

    @GetMapping("/joueur/{joueurId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long joueurId) {
        return ResponseEntity.ok(noteJoueurService.getStats(joueurId));
    }

    @GetMapping("/joueur-du-mois")
    public ResponseEntity<List<Map<String, Object>>> getJoueursDuMois(
            @RequestParam int mois,
            @RequestParam int annee) {
        return ResponseEntity.ok(noteJoueurService.getJoueursDuMois(mois, annee));
    }
}
