package com.nadi.controller;

import com.nadi.dto.AbsenceRequest;
import com.nadi.dto.AbsenceResponse;
import com.nadi.model.Utilisateur;
import com.nadi.security.SecurityUtils;
import com.nadi.service.AbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/presences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<List<AbsenceResponse>> getByCreneauAndDate(
            @RequestParam Long creneauId,
            @RequestParam String date) {
        return ResponseEntity.ok(absenceService.getByCreneauAndDate(creneauId, LocalDate.parse(date)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<List<AbsenceResponse>> savePresences(
            @Valid @RequestBody AbsenceRequest request) {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        return ResponseEntity.ok(absenceService.savePresences(request, user));
    }
}
