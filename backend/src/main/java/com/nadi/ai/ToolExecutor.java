package com.nadi.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadi.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ToolExecutor {

    private final DashboardService dashboardService;
    private final JoueurService joueurService;
    private final PaiementService paiementService;
    private final CreneauService creneauService;
    private final EvenementService evenementService;
    private final AttendanceService attendanceService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ToolExecutor(DashboardService dashboardService, JoueurService joueurService,
                        PaiementService paiementService, CreneauService creneauService,
                        EvenementService evenementService, AttendanceService attendanceService) {
        this.dashboardService = dashboardService;
        this.joueurService = joueurService;
        this.paiementService = paiementService;
        this.creneauService = creneauService;
        this.evenementService = evenementService;
        this.attendanceService = attendanceService;
    }

    public String execute(String toolName, Map<String, Object> args) {
        try {
            return switch (toolName) {
                case "get_player_stats" -> executeGetPlayerStats(args);
                case "get_payment_status" -> executeGetPaymentStatus(args);
                case "get_training_schedule" -> executeGetTrainingSchedule(args);
                case "get_dashboard_summary" -> executeGetDashboardSummary();
                case "get_overdue_payments" -> executeGetOverduePayments();
                case "get_player_list" -> executeGetPlayerList(args);
                case "get_event_list" -> executeGetEventList();
                case "get_convocations" -> executeGetConvocations(args);
                case "get_attendance_stats" -> executeGetAttendanceStats(args);
                case "create_reminder" -> executeCreateReminder(args);
                default -> "{\"error\": \"Outil inconnu: " + toolName + "\"}";
            };
        } catch (Exception e) {
            return "{\"error\": \"Erreur lors de l'exécution: " + e.getMessage() + "\"}";
        }
    }

    private String executeGetPlayerStats(Map<String, Object> args) throws Exception {
        String name = (String) args.getOrDefault("playerName", "");
        var players = joueurService.search(name, PageRequest.of(0, 20)).getContent();
        return mapper.writeValueAsString(Map.of("players", players, "count", players.size()));
    }

    private String executeGetPaymentStatus(Map<String, Object> args) throws Exception {
        String name = (String) args.getOrDefault("playerName", "");
        if (name.isEmpty()) {
            var overdue = paiementService.getOverdue();
            return mapper.writeValueAsString(Map.of("overduePayments", overdue, "count", overdue.size()));
        }
        var players = joueurService.search(name, PageRequest.of(0, 5)).getContent();
        if (!players.isEmpty()) {
            var playerId = players.get(0).getId();
            var payments = paiementService.getByJoueur(playerId, PageRequest.of(0, 20)).getContent();
            return mapper.writeValueAsString(Map.of("payments", payments, "count", payments.size()));
        }
        return mapper.writeValueAsString(Map.of("payments", java.util.List.of(), "count", 0));
    }

    private String executeGetTrainingSchedule(Map<String, Object> args) throws Exception {
        String day = (String) args.getOrDefault("day", null);
        if (day != null && !day.isEmpty()) {
            var slots = creneauService.getByJour(day);
            return mapper.writeValueAsString(Map.of("slots", slots, "count", slots.size()));
        }
        var slots = creneauService.getAll();
        return mapper.writeValueAsString(Map.of("slots", slots, "count", slots.size()));
    }

    private String executeGetDashboardSummary() throws Exception {
        var stats = dashboardService.getStats();
        return mapper.writeValueAsString(stats);
    }

    private String executeGetOverduePayments() throws Exception {
        var overdue = paiementService.getOverdue();
        return mapper.writeValueAsString(Map.of("overduePayments", overdue, "count", overdue.size()));
    }

    private String executeGetPlayerList(Map<String, Object> args) throws Exception {
        var players = joueurService.getAll(PageRequest.of(0, 50)).getContent();
        return mapper.writeValueAsString(Map.of("players", players, "count", players.size()));
    }

    private String executeGetEventList() throws Exception {
        var events = evenementService.getAll();
        return mapper.writeValueAsString(Map.of("events", events, "count", events.size()));
    }

    private String executeGetConvocations(Map<String, Object> args) throws Exception {
        var convocations = evenementService.getMyConvocations();
        return mapper.writeValueAsString(Map.of("convocations", convocations, "count", convocations.size()));
    }

    private String executeGetAttendanceStats(Map<String, Object> args) throws Exception {
        var stats = attendanceService.getGlobalStats();
        return mapper.writeValueAsString(stats);
    }

    private String executeCreateReminder(Map<String, Object> args) throws Exception {
        String playerName = (String) args.get("playerName");
        Double amount = args.containsKey("amount") ? ((Number) args.get("amount")).doubleValue() : null;
        String message = (String) args.getOrDefault("message", "Rappel de paiement");
        return mapper.writeValueAsString(Map.of(
            "success", true,
            "message", "Rappel créé pour " + playerName,
            "amount", amount != null ? amount : "non spécifié",
            "note", "Cette fonctionnalité sera connectée au système de notifications"
        ));
    }
}
