package com.nadi.controller;

import com.nadi.dto.ConsentLogResponse;
import com.nadi.service.ConsentService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rgpd")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConsentController {

    private final ConsentService consentService;

    @GetMapping("/consents")
    public ResponseEntity<List<ConsentLogResponse>> getAll(
            @RequestParam(required = false) Long parentId) {
        if (parentId != null) {
            return ResponseEntity.ok(consentService.getByParent(parentId));
        }
        return ResponseEntity.ok(consentService.getAll());
    }

    @GetMapping("/consents/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(consentService.getStats());
    }

    @PostMapping("/consents")
    public ResponseEntity<ConsentLogResponse> record(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        Long parentId = Long.valueOf(body.get("parentId").toString());
        String type = body.get("type").toString();
        boolean granted = Boolean.parseBoolean(body.get("granted").toString());
        String details = body.getOrDefault("details", "").toString();
        String ipAddress = ip != null ? ip.split(",")[0].trim() : "127.0.0.1";

        return ResponseEntity.ok(consentService.record(parentId, type, granted, details, ipAddress));
    }

    @GetMapping(value = "/consents/export", produces = "application/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        List<ConsentLogResponse> consents = consentService.getAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph("Registre de Consentement RGPD/INPDP", titleFont));
            document.add(new Paragraph("Periode: " + start + " au " + end, bodyFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Date d'export: " + LocalDate.now(), bodyFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 20, 20, 20, 25, 15, 15});

            String[] headers = {"ID", "Parent", "Email", "Type", "Details", "Accorde", "Date"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new java.awt.Color(0x12, 0x2A, 0x22));
                cell.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE)));
                cell.setPadding(4);
                table.addCell(cell);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (ConsentLogResponse c : consents) {
                table.addCell(new Phrase(String.valueOf(c.getId()), bodyFont));
                table.addCell(new Phrase(c.getParentPrenom() + " " + c.getParentNom(), bodyFont));
                table.addCell(new Phrase(c.getParentEmail(), bodyFont));
                table.addCell(new Phrase(formatType(c.getType()), bodyFont));
                table.addCell(new Phrase(c.getDetails() != null ? c.getDetails() : "", bodyFont));
                table.addCell(new Phrase(c.isGranted() ? "Oui" : "Non", bodyFont));
                table.addCell(new Phrase(c.getDateConsentement() != null ? c.getDateConsentement().format(fmt) : "", bodyFont));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Document genere conformement a l'INPDP (Institut National de Protection des Donnees Personnelles) - Tunisie", bodyFont));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation PDF: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "registre_consentement_" + start + "_" + end + ".pdf");

        return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
    }

    private String formatType(String type) {
        return switch (type) {
            case "TRAITEMENT_DONNEES_PERSONNELLES" -> "Traitement donnees";
            case "CONSENTEMENT_IMAGE" -> "Consentement image";
            case "COMMUNICATION_EMAIL" -> "Communication email";
            case "PARTENAIRES_TIERS" -> "Partenaires tiers";
            case "TRANSFERT_DONNEES_HORS_UE" -> "Transfert hors UE";
            default -> type;
        };
    }
}
