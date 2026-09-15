package com.nadi.service;

import com.nadi.dto.DashboardStats;
import com.nadi.model.Document;
import com.nadi.model.StatutDocument;
import com.nadi.model.StatutPaiement;
import com.nadi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final CategorieRepository categorieRepository;
    private final EntraineurRepository entraineurRepository;
    private final PaiementRepository paiementRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        long totalPlayers = joueurRepository.count();
        long pendingPayments = paiementRepository.countByStatut(StatutPaiement.EN_ATTENTE)
                + paiementRepository.countByStatut(StatutPaiement.EN_RETARD);

        LocalDate today = LocalDate.now();
        long expiringDocuments = documentRepository.findExpiringBetween(today, today.plusDays(30)).size();
        long expiredDocuments = documentRepository.findExpiredNotMarked(today).size();

        BigDecimal totalRevenue = paiementRepository.sumPaidBetween(today.withDayOfMonth(1), today);
        BigDecimal pendingAmount = paiementRepository.sumPendingBetween(today.withDayOfMonth(1), today);

        long joueursSansCertificatMedical = joueurRepository.countByCertificatMedicalFalse();
        long joueursSansAutorisationParentale = joueurRepository.countByAutorisationParentaleFalse();

        LocalDate cutoff2Mois = today.minusMonths(2);
        long parentsImpayes2Mois = paiementRepository.findParentIdsWithUnpaidOlderThan(cutoff2Mois).size();

        return DashboardStats.builder()
                .totalPlayers(totalPlayers)
                .activePlayers(totalPlayers)
                .totalParents(parentRepository.count())
                .totalCoaches(entraineurRepository.count())
                .totalCategories(categorieRepository.count())
                .pendingPayments(pendingPayments)
                .complianceAlerts(expiringDocuments + expiredDocuments + joueursSansCertificatMedical + joueursSansAutorisationParentale + parentsImpayes2Mois)
                .expiringDocuments(expiringDocuments)
                .expiredDocuments(expiredDocuments)
                .totalRevenue(totalRevenue)
                .pendingAmount(pendingAmount)
                .joueursSansCertificatMedical(joueursSansCertificatMedical)
                .joueursSansAutorisationParentale(joueursSansAutorisationParentale)
                .parentsImpayes2Mois(parentsImpayes2Mois)
                .build();
    }
}
