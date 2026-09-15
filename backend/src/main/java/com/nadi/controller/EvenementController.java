package com.nadi.controller;

import com.nadi.dto.ConvocationResponse;
import com.nadi.dto.EvenementRequest;
import com.nadi.dto.EvenementResponse;
import com.nadi.service.EvenementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EvenementController {

    private final EvenementService evenementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'PARENT')")
    public ResponseEntity<List<EvenementResponse>> getAll() {
        return ResponseEntity.ok(evenementService.getAll());
    }

    @GetMapping("/month/{year}/{month}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<List<EvenementResponse>> getByMonth(
            @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(evenementService.getByMonth(year, month));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EvenementResponse> create(@Valid @RequestBody EvenementRequest request) {
        EvenementResponse response = evenementService.create(request);
        return ResponseEntity.created(URI.create("/events/" + response.getId())).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EvenementResponse> update(@PathVariable Long id, @RequestBody EvenementRequest request) {
        return ResponseEntity.ok(evenementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        evenementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/convocations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<List<ConvocationResponse>> getConvocations(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.getConvocations(id));
    }

    @GetMapping("/my-convocations")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ConvocationResponse>> getMyConvocations() {
        return ResponseEntity.ok(evenementService.getMyConvocations());
    }

    @PostMapping("/convocations/{id}/respond")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ConvocationResponse> respond(
            @PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean accept = Boolean.TRUE.equals(body.get("accept"));
        return ResponseEntity.ok(evenementService.respondToConvocation(id, accept));
    }
}
