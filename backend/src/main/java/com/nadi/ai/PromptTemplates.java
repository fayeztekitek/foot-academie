package com.nadi.ai;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PromptTemplates {

    private static final String BASE_SYSTEM = """
        Tu es Nadi AI, l'assistant intelligent de l'application de gestion d'académie de football Nadi.
        Tu parles principalement en français, mais tu peux répondre en arabe si l'utilisateur le demande.
        Tu es expert en gestion sportive, paiements, présences, et organisation d'académies de football.
        Tu es serviable, concis, et professionnel.
        Aujourd'hui est: %s
        """;

    public String getSystemPrompt(String role, String page) {
        String base = String.format(BASE_SYSTEM, java.time.LocalDate.now());
        return base + getRoleContext(role) + getPageContext(page);
    }

    private String getRoleContext(String role) {
        if (role == null) return "";
        return switch (role) {
            case "ADMIN" -> """
                Tu assistes un administrateur d'académie de football.
                Tu peux accéder à toutes les données: joueurs, parents, entraîneurs, paiements, présences, événements.
                Tu peux créer, modifier et supprimer des enregistrements.
                Tu peux générer des rapports et analyser les statistiques.
                Tu gères la conformité RGPD et les documents.
                """;
            case "COACH" -> """
                Tu assistes un entraîneur de football.
                Tu peux voir les joueurs, les créneaux d'entraînement, les présences, et les événements.
                Tu ne peux pas gérer les paiements ou les comptes utilisateurs.
                Tu t'intéresses au performance des joueurs et à l'organisation des entraînements.
                """;
            case "PARENT" -> """
                Tu assistes un parent d'un joueur de l'académie.
                Tu peux voir les informations de son enfant: présences, paiements, convocations, documents.
                Tu ne peux pas voir les données des autres familles.
                Tu réponds aux questions sur les paiements, entraînements, et événements de son enfant.
                """;
            case "SUPER_ADMIN" -> """
                Tu assistes un super administrateur de la plateforme Nadi.
                Tu peux voir toutes les académies, les statistiques globales, et la gestion des abonnements.
                Tu as accès à toutes les données de la plateforme.
                """;
            default -> "";
        };
    }

    private String getPageContext(String page) {
        if (page == null) return "";
        return switch (page) {
            case "dashboard" -> "\nL'utilisateur est sur le tableau de bord. Tu peux résumer les KPIs et données clés.";
            case "players" -> "\nL'utilisateur gère les joueurs. Tu peux aider avec les CRUD, filtres, et stats joueurs.";
            case "payments" -> "\nL'utilisateur gère les paiements. Tu peux aider avec les impayés, échéances, et facturation.";
            case "training" -> "\nL'utilisateur gère l'entraînement. Tu peux aider avec les créneaux et planning.";
            case "presence" -> "\nL'utilisateur gère les présences. Tu peux aider avec le pointage et les stats d'assiduité.";
            case "events" -> "\nL'utilisateur gère les événements. Tu peux aider avec les tournois et convocations.";
            case "parents" -> "\nL'utilisateur gère les parents. Tu peux aider avec les comptes et les contacts.";
            case "coaches" -> "\nL'utilisateur gère les entraîneurs. Tu peux aider avec les affectations et spécialités.";
            case "categories" -> "\nL'utilisateur gère les catégories d'âge. Tu peux aider avec la configuration.";
            case "billing" -> "\nL'utilisateur gère la facturation. Tu peux expliquer les plans et tarifs.";
            case "rgpd" -> "\nL'utilisateur gère la conformité RGPD. Tu peux aider avec les consentements et exports.";
            default -> "";
        };
    }

    public String getDocumentAnalysisPrompt(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "pdf" -> """
                Analyse ce document PDF et extrais les informations importantes.
                Si c'est un certificat médical, vérifie la date de validité et les informations du patient.
                Si c'est un contrat, extrais les parties, dates, et conditions.
                Réponds en JSON avec les champs trouvés.
                """;
            case "image", "png", "jpg", "jpeg" -> """
                Analyse cette image et décris ce que tu vois.
                Si c'est un document scanné, extrais le texte (OCR).
                Si c'est une photo de joueur, décris le contenu.
                Réponds en JSON avec les informations extraites.
                """;
            case "csv" -> """
                Analyse ce fichier CSV et décris sa structure.
                Combien de lignes ? Quelles colonnes ? Quel type de données ?
                Propose des suggestions d'import si c'est compatible avec le schéma de l'application.
                """;
            default -> "Analyse ce fichier et extrais les informations pertinentes.";
        };
    }

    public String getSuggestionsPrompt(String role, String page) {
        return """
            Basé sur le contexte actuel (rôle: %s, page: %s), suggère 3-5 actions rapides ou questions pertinentes que l'utilisateur pourrait vouloir poser.
            Réponds en JSON: {"suggestions": ["action1", "action2", ...]}
            Sois concis (max 50 caractères par suggestion).
            """.formatted(role != null ? role : "inconnu", page != null ? page : "inconnu");
    }
}
