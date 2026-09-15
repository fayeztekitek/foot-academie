package com.nadi.service;

import com.nadi.dto.SuperAdminStatsResponse;
import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminStatsService {

    private final AcademieRepository academieRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    public SuperAdminStatsResponse getStats() {
        List<Academie> allAcademies = academieRepository.findAll();

        long totalUsers = utilisateurRepository.count();
        long totalPlayers = allAcademies.stream()
                .mapToLong(a -> safeCount(() -> joueurRepository.countByTenantId(a.getId())))
                .sum();
        long totalParents = allAcademies.stream()
                .mapToLong(a -> safeCount(() -> parentRepository.countByTenantId(a.getId())))
                .sum();
        long totalCoaches = allAcademies.stream()
                .mapToLong(a -> safeCount(() -> entraineurRepository.countByTenantId(a.getId())))
                .sum();

        List<SuperAdminStatsResponse.AcademieStats> academieStats = new ArrayList<>();
        for (Academie academie : allAcademies) {
            long joueurCount = safeCount(() -> joueurRepository.countByTenantId(academie.getId()));
            long parentCount = safeCount(() -> parentRepository.countByTenantId(academie.getId()));
            long coachCount = safeCount(() -> entraineurRepository.countByTenantId(academie.getId()));

            academieStats.add(SuperAdminStatsResponse.AcademieStats.builder()
                    .id(academie.getId())
                    .nom(academie.getNom())
                    .slug(academie.getSlug())
                    .ville(academie.getVille())
                    .plan(academie.getPlan())
                    .active(academie.getActive())
                    .joueurCount(joueurCount)
                    .parentCount(parentCount)
                    .coachCount(coachCount)
                    .paymentCount(0)
                    .revenue(0)
                    .build());
        }

        Map<String, Long> byPlan = allAcademies.stream()
                .collect(Collectors.groupingBy(a -> a.getPlan() != null ? a.getPlan() : "FREE", Collectors.counting()));

        List<SuperAdminStatsResponse.PlanStats> planStats = byPlan.entrySet().stream()
                .map(e -> SuperAdminStatsResponse.PlanStats.builder()
                        .plan(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return SuperAdminStatsResponse.builder()
                .totalAcademies(allAcademies.size())
                .activeAcademies(allAcademies.stream().filter(Academie::getActive).count())
                .inactiveAcademies(allAcademies.stream().filter(a -> !a.getActive()).count())
                .totalUsers(totalUsers)
                .totalPlayers(totalPlayers)
                .totalParents(totalParents)
                .totalCoaches(totalCoaches)
                .academies(academieStats)
                .byPlan(planStats)
                .build();
    }

    private long safeCount(RunnableChecked action) {
        try {
            action.run();
        } catch (Exception ignored) {
        }
        return 0;
    }

    @FunctionalInterface
    private interface RunnableChecked {
        void run() throws Exception;
    }

    private long safeCount(java.util.function.Supplier<Long> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return 0;
        }
    }
}
