package com.nadi.controller;

import com.nadi.model.ConsentementRGPD;
import com.nadi.service.ConsentementRGPDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rgpd")
@RequiredArgsConstructor
public class ConsentementRGPDController {

    private final ConsentementRGPDService consentementService;

    @GetMapping("/consent-register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConsentementRGPD>> getAll() {
        return ResponseEntity.ok(consentementService.getAll());
    }

    @GetMapping("/consent-register/parent/{parentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<List<ConsentementRGPD>> getByParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(consentementService.getByParent(parentId));
    }

    @PostMapping("/consent-register")
    public ResponseEntity<ConsentementRGPD> record(@RequestBody Map<String, Object> body) {
        Long parentId = Long.valueOf(body.get("parentId").toString());
        String type = (String) body.get("type");
        boolean accord = (Boolean) body.get("accord");
        String ip = (String) body.getOrDefault("ipAddress", "unknown");
        String details = (String) body.getOrDefault("details", null);
        return ResponseEntity.ok(consentementService.record(parentId, type, accord, ip, details));
    }

    @GetMapping("/consent-register/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportCsv() {
        String csv = consentementService.exportCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "registre_consentement_rgpd.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
