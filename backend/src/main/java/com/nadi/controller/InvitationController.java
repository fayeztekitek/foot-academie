package com.nadi.controller;

import com.nadi.dto.AcceptInvitationRequest;
import com.nadi.dto.InvitationRequest;
import com.nadi.dto.InvitationResponse;
import com.nadi.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> create(@Valid @RequestBody InvitationRequest request) {
        return ResponseEntity.ok(invitationService.createInvitation(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponse>> listAll() {
        return ResponseEntity.ok(invitationService.listAll());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponse>> listPending() {
        return ResponseEntity.ok(invitationService.listPending());
    }

    @GetMapping("/by-token/{token}")
    public ResponseEntity<InvitationResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.getByToken(token));
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        invitationService.acceptInvitation(request);
        return ResponseEntity.ok(Map.of("message", "Invitation acceptée avec succès"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id) {
        invitationService.cancelInvitation(id);
        return ResponseEntity.ok(Map.of("message", "Invitation annulée"));
    }
}
