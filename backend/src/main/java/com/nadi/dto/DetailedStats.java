package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedStats {

    private long totalPlayers;
    private long totalParents;
    private long totalCoaches;
    private long totalCategories;

    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLastMonth;
    private BigDecimal pendingAmount;
    private long paidPaymentsThisMonth;
    private long unpaidPayments;

    private Map<String, Long> playersByCategory;
    private Map<String, Long> paymentsByStatus;

    private List<MonthlyRevenue> revenueHistory;
    private List<AttendanceEntry> attendanceSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
        private BigDecimal pending;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceEntry {
        private String categoryName;
        private double attendanceRate;
        private long totalSessions;
        private long totalPresences;
    }
}
