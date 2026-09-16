package com.nadi.ai;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ToolRegistry {

    private final List<Map<String, Object>> tools = new ArrayList<>();

    public ToolRegistry() {
        registerTool("get_player_stats",
            "Obtenir les statistiques d'un joueur (nom, catégorie, paiements, présences)",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "playerName", Map.of("type", "string", "description", "Nom ou prénom du joueur")
                ),
                "required", List.of("playerName")
            ));

        registerTool("get_payment_status",
            "Vérifier le statut de paiement d'un joueur ou de tous les joueurs en retard",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "playerName", Map.of("type", "string", "description", "Nom du joueur (optionnel, vide = tous les impayés)")
                )
            ));

        registerTool("get_training_schedule",
            "Obtenir les créneaux d'entraînement par jour ou catégorie",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "day", Map.of("type", "string", "description", "Jour de la semaine (lundi, mardi...)"),
                    "category", Map.of("type", "string", "description", "Nom de la catégorie (U7, U9, U11...)")
                )
            ));

        registerTool("get_dashboard_summary",
            "Obtenir un résumé complet du tableau de bord avec tous les KPIs",
            Map.of("type", "object", "properties", Map.of()));

        registerTool("get_overdue_payments",
            "Lister tous les paiements en retard avec les détails",
            Map.of("type", "object", "properties", Map.of()));

        registerTool("get_player_list",
            "Lister les joueurs avec filtres optionnels",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "category", Map.of("type", "string", "description", "Filtrer par catégorie"),
                    "paymentStatus", Map.of("type", "string", "description", "Filtrer par statut: A_JOUR, EN_ATTENTE, EN_RETARD")
                )
            ));

        registerTool("get_event_list",
            "Lister les événements à venir",
            Map.of("type", "object", "properties", Map.of()));

        registerTool("get_convocations",
            "Obtenir les convocations d'un joueur ou de tous les joueurs",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "playerName", Map.of("type", "string", "description", "Nom du joueur")
                )
            ));

        registerTool("get_attendance_stats",
            "Obtenir les statistiques de présence (taux de présence par joueur)",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "period", Map.of("type", "string", "description", "Période: mois, trimestre, semestre, annee")
                )
            ));

        registerTool("create_reminder",
            "Créer un rappel pour un paiement en retard",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "playerName", Map.of("type", "string", "description", "Nom du joueur"),
                    "amount", Map.of("type", "number", "description", "Montant dû en TND"),
                    "message", Map.of("type", "string", "description", "Message du rappel")
                ),
                "required", List.of("playerName")
            ));
    }

    private void registerTool(String name, String description, Map<String, Object> parameters) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        tool.put("parameters", parameters);
        tools.add(tool);
    }

    public List<Map<String, Object>> getTools() {
        return Collections.unmodifiableList(tools);
    }

    public List<Map<String, Object>> getToolsForRole(String role) {
        if ("PARENT".equals(role)) {
            return tools.stream()
                .filter(t -> Set.of("get_player_stats", "get_payment_status", "get_training_schedule",
                    "get_convocations", "get_event_list").contains(t.get("name")))
                .toList();
        }
        if ("COACH".equals(role)) {
            return tools.stream()
                .filter(t -> !Set.of("get_dashboard_summary", "create_reminder").contains(t.get("name")))
                .toList();
        }
        return tools;
    }
}
