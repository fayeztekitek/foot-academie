package com.nadi.controller;

import com.nadi.dto.DetailedStats;
import com.nadi.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/detailed")
    public ResponseEntity<DetailedStats> getDetailed() {
        return ResponseEntity.ok(statsService.getDetailedStats());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv() {
        DetailedStats stats = statsService.getDetailedStats();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringWriter writer = new StringWriter();

        writer.write("Statistique,Valeur\n");
        writer.write("Total joueurs," + stats.getTotalPlayers() + "\n");
        writer.write("Total parents," + stats.getTotalParents() + "\n");
        writer.write("Total entraîneurs," + stats.getTotalCoaches() + "\n");
        writer.write("Total catégories," + stats.getTotalCategories() + "\n");
        writer.write("Revenus ce mois," + stats.getRevenueThisMonth() + " TND\n");
        writer.write("Revenus mois dernier," + stats.getRevenueLastMonth() + " TND\n");
        writer.write("Montant en attente," + stats.getPendingAmount() + " TND\n");
        writer.write("Paiements payés ce mois," + stats.getPaidPaymentsThisMonth() + "\n");
        writer.write("Paiements impayés," + stats.getUnpaidPayments() + "\n");

        writer.write("\nJoueurs par catégorie\n");
        stats.getPlayersByCategory().forEach((cat, count) ->
                writer.write(cat + "," + count + "\n"));

        writer.write("\nRevenus mensuels\n");
        writer.write("Mois,Revenus,En attente\n");
        stats.getRevenueHistory().forEach(r ->
                writer.write(r.getMonth() + "," + r.getRevenue() + "," + r.getPending() + "\n"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment",
                "statistiques_" + LocalDate.now().format(fmt) + ".csv");
        return ResponseEntity.ok().headers(headers).body(writer.toString());
    }
}
