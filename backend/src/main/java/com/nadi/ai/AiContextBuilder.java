package com.nadi.ai;

import com.nadi.model.Utilisateur;
import com.nadi.security.SecurityUtils;
import com.nadi.service.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AiContextBuilder {

    private final JoueurService joueurService;
    private final PaiementService paiementService;
    private final CreneauService creneauService;
    private final DashboardService dashboardService;
    private final ParentService parentService;
    private final EntraineurService entraineurService;
    private final CategorieService categorieService;
    private final SecurityUtils securityUtils;

    public AiContextBuilder(JoueurService joueurService, PaiementService paiementService,
                            CreneauService creneauService, DashboardService dashboardService,
                            ParentService parentService, EntraineurService entraineurService,
                            CategorieService categorieService, SecurityUtils securityUtils) {
        this.joueurService = joueurService;
        this.paiementService = paiementService;
        this.creneauService = creneauService;
        this.dashboardService = dashboardService;
        this.parentService = parentService;
        this.entraineurService = entraineurService;
        this.categorieService = categorieService;
        this.securityUtils = securityUtils;
    }

    public Map<String, Object> buildContext(String page) {
        Map<String, Object> context = new HashMap<>();
        try {
            Utilisateur user = securityUtils.getCurrentUserOrThrow();
            context.put("userRole", user.getRole().name());
            context.put("userEmail", user.getEmail());
            context.put("page", page);

            if ("ADMIN".equals(user.getRole().name()) || "SUPER_ADMIN".equals(user.getRole().name())) {
                context.put("totalPlayers", joueurService.getAll(PageRequest.of(0, 1)).getTotalElements());
                context.put("overduePayments", paiementService.getOverdue().size());
                context.put("activeCategories", categorieService.getAll(PageRequest.of(0, 1)).getTotalElements());
                context.put("allCreneaux", creneauService.getAll().size());
            }

            if ("COACH".equals(user.getRole().name())) {
                context.put("allSlots", creneauService.getAll().size());
            }

            if ("PARENT".equals(user.getRole().name())) {
                context.put("myChildren", joueurService.getByParent(user.getId(), PageRequest.of(0, 100)).getTotalElements());
                context.put("myPayments", paiementService.getByParent(user.getId(), PageRequest.of(0, 100)).getTotalElements());
            }
        } catch (Exception e) {
            context.put("error", "Impossible de charger le contexte: " + e.getMessage());
        }
        return context;
    }

    public String buildContextString(String page) {
        Map<String, Object> ctx = buildContext(page);
        StringBuilder sb = new StringBuilder();
        sb.append("Contexte actuel:\n");
        ctx.forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }
}
