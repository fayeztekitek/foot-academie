package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    private long totalPlayers;
    private long activePlayers;
    private long totalParents;
    private long totalCoaches;
    private long totalCategories;
    private long pendingPayments;
    private long complianceAlerts;
    private long expiringDocuments;
    private long expiredDocuments;
    private BigDecimal totalRevenue;
    private BigDecimal pendingAmount;
    private long joueursSansCertificatMedical;
    private long joueursSansAutorisationParentale;
    private long parentsImpayes2Mois;
}
