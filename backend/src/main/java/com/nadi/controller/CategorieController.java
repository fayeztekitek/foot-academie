package com.nadi.controller;

import com.nadi.dto.CategorieRequest;
import com.nadi.dto.CategorieResponse;
import com.nadi.service.CategorieService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<Page<CategorieResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        Page<CategorieResponse> result = (search != null && !search.isBlank())
                ? categorieService.search(search, pageable)
                : categorieService.getAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<CategorieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categorieService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategorieResponse> create(@Valid @RequestBody CategorieRequest request) {
        CategorieResponse response = categorieService.create(request);
        return ResponseEntity.created(URI.create("/categories/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategorieResponse> update(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        categorieService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Catégorie supprimée"));
    }
}
