package com.nadi.controller;

import com.nadi.dto.AcademieRequest;
import com.nadi.dto.AcademieResponse;
import com.nadi.service.AcademieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/academies")
@RequiredArgsConstructor
public class AcademieController {

    private final AcademieService academieService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AcademieResponse>> listAll(Pageable pageable) {
        return ResponseEntity.ok(academieService.listAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AcademieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(academieService.getById(id));
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('PARENT')")
    public ResponseEntity<AcademieResponse> getCurrentTenantInfo() {
        return ResponseEntity.ok(academieService.getCurrentTenantInfo());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AcademieResponse> create(@Valid @RequestBody AcademieRequest request) {
        return ResponseEntity.ok(academieService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AcademieResponse> update(@PathVariable Long id, @Valid @RequestBody AcademieRequest request) {
        return ResponseEntity.ok(academieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        academieService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Académie supprimée avec succès"));
    }

    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AcademieResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(academieService.toggleActive(id));
    }

    @PostMapping("/{id}/deactivate-if-unpaid")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AcademieResponse> deactivateIfUnpaid(@PathVariable Long id) {
        return ResponseEntity.ok(academieService.deactivateIfUnpaid(id));
    }
}
