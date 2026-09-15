package com.nadi.service;

import com.nadi.dto.DetailedStats;
import com.nadi.model.StatutPaiement;
import com.nadi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;
    private final CategorieRepository categorieRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public DetailedStats getDetailedStats() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDate currentStart = currentMonth.atDay(1);
        LocalDate currentEnd = currentMonth.atEndOfMonth();
        LocalDate lastStart = lastMonth.atDay(1);
        LocalDate lastEnd = lastMonth.atEndOfMonth();

        BigDecimal revenueThisMonth = paiementRepository.sumPaidBetween(currentStart, currentEnd);
        BigDecimal revenueLastMonth = paiementRepository.sumPaidBetween(lastStart, lastEnd);
        BigDecimal pendingAmount = paiementRepository.sumPendingBetween(currentStart, currentEnd);

        Map<String, Long> playersByCategory = joueurRepository.findAll().stream()
                .filter(j -> j.getCategorie() != null)
                .collect(Collectors.groupingBy(
                        j -> j.getCategorie().getNom(),
                        Collectors.counting()));

        Map<String, Long> paymentsByStatus = new LinkedHashMap<>();
        for (StatutPaiement s : StatutPaiement.values()) {
            paymentsByStatus.put(s.name(), paiementRepository.countByStatut(s));
        }

        List<DetailedStats.MonthlyRevenue> revenueHistory = getRevenueHistory(6);

        return DetailedStats.builder()
                .totalPlayers(joueurRepository.count())
                .totalParents(parentRepository.count())
                .totalCoaches(entraineurRepository.count())
                .totalCategories(categorieRepository.count())
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .pendingAmount(pendingAmount)
                .paidPaymentsThisMonth(paiementRepository.countByStatut(StatutPaiement.PAYE))
                .unpaidPayments(paiementRepository.countByStatut(StatutPaiement.EN_ATTENTE)
                        + paiementRepository.countByStatut(StatutPaiement.EN_RETARD))
                .playersByCategory(playersByCategory)
                .paymentsByStatus(paymentsByStatus)
                .revenueHistory(revenueHistory)
                .attendanceSummary(List.of())
                .build();
    }

    private List<DetailedStats.MonthlyRevenue> getRevenueHistory(int months) {
        YearMonth current = YearMonth.now();
        return java.util.stream.LongStream.range(0, months)
                .mapToObj(i -> current.minusMonths(months - 1 - i))
                .map(ym -> new DetailedStats.MonthlyRevenue(
                        ym.toString(),
                        paiementRepository.sumPaidBetween(ym.atDay(1), ym.atEndOfMonth()),
                        paiementRepository.sumPendingBetween(ym.atDay(1), ym.atEndOfMonth())))
                .collect(Collectors.toList());
    }
}
