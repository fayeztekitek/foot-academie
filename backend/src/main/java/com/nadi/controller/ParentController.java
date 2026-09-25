package com.nadi.controller;

import com.nadi.dto.ParentRequest;
import com.nadi.dto.ParentResponse;
import com.nadi.model.Parent;
import com.nadi.model.Role;
import com.nadi.model.Utilisateur;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Page<ParentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        Page<ParentResponse> result = (search != null && !search.isBlank())
                ? parentService.search(search, pageable)
                : parentService.getAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParentResponse> getById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isParent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PARENT"));

        if (isParent) {
            Utilisateur user;
            if (auth.getPrincipal() instanceof Utilisateur) {
                user = (Utilisateur) auth.getPrincipal();
            } else {
                user = utilisateurRepository.findByEmail(auth.getName())
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            }
            ParentResponse myProfile = parentService.getByUtilisateurId(user.getId());
            if (!myProfile.getId().equals(id)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(parentService.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ParentResponse> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Utilisateur user;
        if (auth.getPrincipal() instanceof Utilisateur) {
            user = (Utilisateur) auth.getPrincipal();
        } else {
            user = utilisateurRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }
        return ResponseEntity.ok(parentService.getByUtilisateurId(user.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParentResponse> create(@Valid @RequestBody ParentRequest request) {
        ParentResponse response = parentService.create(request);
        return ResponseEntity.created(URI.create("/parents/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParentResponse> update(@PathVariable Long id, @Valid @RequestBody ParentRequest request) {
        return ResponseEntity.ok(parentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        parentService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Parent supprimé"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nouveauMotDePasse = body.get("motDePasse");
        try {
            com.nadi.security.PasswordPolicy.validateOrThrow(nouveauMotDePasse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        parentService.resetPassword(id, nouveauMotDePasse);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}
