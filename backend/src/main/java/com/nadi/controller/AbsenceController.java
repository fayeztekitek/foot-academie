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

    @GetMapping("/mes-absences")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<AbsenceResponse>> getMyUpcomingAbsences() {
        Long userId = securityUtils.getCurrentUserId();
        Long parentId = absenceService.resolveOwnParentId(userId);
        return ResponseEntity.ok(absenceService.getUpcomingByParent(parentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<List<AbsenceResponse>> savePresences(
            @Valid @RequestBody AbsenceRequest request) {
        Utilisateur user = securityUtils.getCurrentUserOrThrow();
        return ResponseEntity.ok(absenceService.savePresences(request, user));
    }

    @PostMapping("/declarer")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<AbsenceResponse> declareAbsence(@RequestBody java.util.Map<String, Object> body) {
        if (body.get("joueurId") == null || body.get("creneauId") == null || body.get("dateSeance") == null) {
            return ResponseEntity.badRequest().build();
        }
        Long joueurId = Long.valueOf(body.get("joueurId").toString());
        Long creneauId = Long.valueOf(body.get("creneauId").toString());
        LocalDate dateSeance = LocalDate.parse(body.get("dateSeance").toString());
        String motif = body.get("motif") != null ? body.get("motif").toString() : null;
        return ResponseEntity.ok(absenceService.declareAbsence(joueurId, creneauId, dateSeance, motif));
    }
}
