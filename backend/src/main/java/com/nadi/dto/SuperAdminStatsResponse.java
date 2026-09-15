package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdminStatsResponse {

    private long totalAcademies;
    private long activeAcademies;
    private long inactiveAcademies;
    private long totalUsers;
    private long totalPlayers;
    private long totalParents;
    private long totalCoaches;
    private long totalPayments;
    private long totalRevenue;
    private List<AcademieStats> academies;
    private List<PlanStats> byPlan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcademieStats {
        private Long id;
        private String nom;
        private String slug;
        private String ville;
        private String plan;
        private Boolean active;
        private long joueurCount;
        private long parentCount;
        private long coachCount;
        private long paymentCount;
        private long revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanStats {
        private String plan;
        private long count;
    }
}
