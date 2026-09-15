package com.nadi.controller;

import com.nadi.dto.SuperAdminStatsResponse;
import com.nadi.service.SuperAdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats/super-admin")
@RequiredArgsConstructor
public class SuperAdminStatsController {

    private final SuperAdminStatsService superAdminStatsService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SuperAdminStatsResponse> getStats() {
        return ResponseEntity.ok(superAdminStatsService.getStats());
    }
}
