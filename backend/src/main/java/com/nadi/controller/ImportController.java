package com.nadi.controller;

import com.nadi.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {

    private final DataImportService importService;

    @PostMapping("/players")
    public ResponseEntity<DataImportService.ImportResult> importPlayers(
            @RequestParam("file") MultipartFile file) throws IOException {
        DataImportService.ImportResult result = importService.importPlayersCsv(file.getInputStream());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payments")
    public ResponseEntity<DataImportService.ImportResult> importPayments(
            @RequestParam("file") MultipartFile file) throws IOException {
        DataImportService.ImportResult result = importService.importPaymentsCsv(file.getInputStream());
        return ResponseEntity.ok(result);
    }
}
