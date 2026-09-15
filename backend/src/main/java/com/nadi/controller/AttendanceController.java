package com.nadi.controller;

import com.nadi.dto.PresenceStatsByJoueurResponse;
import com.nadi.dto.PresenceStatsGlobalResponse;
import com.nadi.model.Parent;
import com.nadi.repository.ParentRepository;
import com.nadi.security.SecurityUtils;
import com.nadi.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final SecurityUtils securityUtils;
    private final ParentRepository parentRepository;

    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PresenceStatsGlobalResponse> getGlobalStats() {
        return ResponseEntity.ok(attendanceService.getGlobalStats());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<List<PresenceStatsByJoueurResponse>> getAllJoueurStats() {
        return ResponseEntity.ok(attendanceService.getAllJoueurStats());
    }

    @GetMapping("/joueur/{joueurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<PresenceStatsByJoueurResponse> getStatsByJoueur(@PathVariable Long joueurId) {
        return ResponseEntity.ok(attendanceService.getStatsByJoueur(joueurId));
    }

    @GetMapping("/my-children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<PresenceStatsByJoueurResponse>> getMyChildrenStats() {
        Long userId = securityUtils.getCurrentUserId();
        Parent parent = parentRepository.findByUtilisateurId(userId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé"));
        return ResponseEntity.ok(attendanceService.getStatsByParent(parent.getId()));
    }
}
