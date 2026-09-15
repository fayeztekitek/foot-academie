package com.nadi.controller;

import com.nadi.model.Paiement;
import com.nadi.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final PaiementRepository paiementRepository;

    @GetMapping(value = "/payments", produces = "text/csv")
    public ResponseEntity<String> exportPaymentsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<Paiement> paiements = paiementRepository.findByDateRange(start, end);

        StringWriter writer = new StringWriter();
        writer.write("ID,Joueur,Prenom Joueur,Parent,Prenom Parent,Montant,Devise,Date Echeance,Date Paiement,Statut,Moyen Paiement,Formule,Reference\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Paiement p : paiements) {
            writer.write(String.format("%d,%s %s,%s %s,%.3f,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    p.getId(),
                    p.getJoueur().getNom(), p.getJoueur().getPrenom(),
                    p.getParent().getNom(), p.getParent().getPrenom(),
                    p.getMontant(), p.getDevise(),
                    p.getDateEcheance().format(fmt),
                    p.getDatePaiement() != null ? p.getDatePaiement().format(fmt) : "",
                    p.getStatut().name(),
                    p.getMoyenPaiement() != null ? p.getMoyenPaiement().name() : "",
                    p.getFormule() != null ? p.getFormule().name() : "",
                    p.getReference() != null ? p.getReference() : ""
            ));
        }

        String csvContent = writer.toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment",
                "paiements_" + start.format(fmt) + "_" + end.format(fmt) + ".csv");

        return ResponseEntity.ok().headers(headers).body(csvContent);
    }

    @GetMapping("/payments/excel")
    public ResponseEntity<byte[]> exportPaymentsExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<Paiement> paiements = paiementRepository.findByDateRange(start, end);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>\n");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
        xml.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        xml.append("<Worksheet ss:Name=\"Paiements\">\n<Table>\n");

        xml.append("<Row>");
        for (String h : List.of("ID", "Joueur", "Parent", "Montant", "Devise", "Échéance", "Paiement", "Statut", "Moyen", "Formule", "Référence")) {
            xml.append("<Cell><Data ss:Type=\"String\">").append(h).append("</Data></Cell>");
        }
        xml.append("</Row>\n");

        for (Paiement p : paiements) {
            xml.append("<Row>");
            xml.append(cell(String.valueOf(p.getId())));
            xml.append(cell(p.getJoueur().getPrenom() + " " + p.getJoueur().getNom()));
            xml.append(cell(p.getParent().getPrenom() + " " + p.getParent().getNom()));
            xml.append(cell(p.getMontant().toString()));
            xml.append(cell(p.getDevise()));
            xml.append(cell(p.getDateEcheance().format(fmt)));
            xml.append(cell(p.getDatePaiement() != null ? p.getDatePaiement().format(fmt) : ""));
            xml.append(cell(p.getStatut().name()));
            xml.append(cell(p.getMoyenPaiement() != null ? p.getMoyenPaiement().name() : ""));
            xml.append(cell(p.getFormule() != null ? p.getFormule().name() : ""));
            xml.append(cell(p.getReference() != null ? p.getReference() : ""));
            xml.append("</Row>\n");
        }

        xml.append("</Table>\n</Worksheet>\n</Workbook>");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "paiements_" + start.format(fmt) + "_" + end.format(fmt) + ".xls");

        return ResponseEntity.ok().headers(headers).body(xml.toString().getBytes());
    }

    private String cell(String value) {
        return "<Cell><Data ss:Type=\"String\">" + (value != null ? value : "") + "</Data></Cell>";
    }
}
