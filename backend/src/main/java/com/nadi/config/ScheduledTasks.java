package com.nadi.config;

import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.service.DocumentService;
import com.nadi.service.NotificationService;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ScheduledTasks {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final JoueurRepository joueurRepository;
    private final PaiementRepository paiementRepository;
    private final AcademieRepository academieRepository;

    private static final BigDecimal MONTANT_MENSUEL = new BigDecimal("60.000");

    @Scheduled(cron = "0 0 7 * * *")
    public void checkDocumentExpiry() {
        log.info("Checking document expiry...");
        List<Long> allTenantIds = academieRepository.findAll().stream()
                .map(Academie::getId)
                .toList();

        for (Long tenantId : allTenantIds) {
            try {
                TenantContext.setTenantId(tenantId);
                documentService.updateExpiryStatuses();

                List<Document> expiringSoon = documentRepository.findExpiringBetween(
                        LocalDate.now(), LocalDate.now().plusDays(30));

                List<Utilisateur> admins = utilisateurRepository.findAll().stream()
                        .filter(u -> u.getRole().name().equals("ADMIN"))
                        .toList();

                for (Document doc : expiringSoon) {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), doc.getDateExpiration());
                    String message = String.format("%s de %s %s expire dans %d jour(s)",
                            doc.getType().name(),
                            doc.getJoueur().getPrenom(),
                            doc.getJoueur().getNom(),
                            daysLeft);

                    for (Utilisateur admin : admins) {
                        notificationService.create(admin, Notification.TypeNotification.DOCUMENT_EXPIRE_BIENTOT, message, null);
                    }
                }

                List<Document> expired = documentRepository.findExpiredNotMarked(LocalDate.now());
                for (Document doc : expired) {
                    String message = String.format("%s de %s %s est EXPIRÉ",
                            doc.getType().name(),
                            doc.getJoueur().getPrenom(),
                            doc.getJoueur().getNom());

                    for (Utilisateur admin : admins) {
                        notificationService.create(admin, Notification.TypeNotification.DOCUMENT_EXPIRE, message, null);
                    }
                }

                log.info("Tenant {}: Document expiry check complete. {} expiring, {} expired.",
                        tenantId, expiringSoon.size(), expired.size());
            } catch (Exception e) {
                log.error("Error checking document expiry for tenant {}: {}", tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }

    @Scheduled(cron = "0 5 0 1 * *")
    public void generateMonthlyPayments() {
        log.info("Generating monthly payments for active players...");
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        List<Long> allTenantIds = academieRepository.findAll().stream()
                .map(Academie::getId)
                .toList();

        for (Long tenantId : allTenantIds) {
            try {
                TenantContext.setTenantId(tenantId);
                int created = 0;

                List<Joueur> allPlayers = joueurRepository.findAll();
                for (Joueur joueur : allPlayers) {
                    if (joueur.getParent() == null) continue;
                    if (joueur.getDateEntree() == null) continue;
                    if (joueur.getDateEntree().isAfter(monthEnd)) continue;
                    if (joueur.getFrequence() == null) continue;

                    boolean alreadyExists = paiementRepository
                            .findByJoueurIdAndDateEcheanceBetween(joueur.getId(), monthStart, monthEnd)
                            .stream().anyMatch(p -> p.getStatut() != StatutPaiement.ANNULE);

                    if (alreadyExists) continue;

                    Paiement paiement = Paiement.builder()
                            .joueur(joueur)
                            .parent(joueur.getParent())
                            .montant(MONTANT_MENSUEL)
                            .devise("TND")
                            .dateEcheance(monthStart)
                            .statut(StatutPaiement.EN_ATTENTE)
                            .formule(joueur.getFrequence())
                            .build();
                    paiementRepository.save(paiement);
                    created++;
                }

                log.info("Tenant {}: Monthly payments generated: {} new payments for {}", tenantId, created, currentMonth);
            } catch (Exception e) {
                log.error("Error generating monthly payments for tenant {}: {}", tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void checkOverduePayments() {
        log.info("Checking for overdue payments...");
        LocalDate today = LocalDate.now();

        List<Long> allTenantIds = academieRepository.findAll().stream()
                .map(Academie::getId)
                .toList();

        for (Long tenantId : allTenantIds) {
            try {
                TenantContext.setTenantId(tenantId);
                List<Paiement> overdue = paiementRepository.findByDateEcheanceBeforeAndStatut(today, StatutPaiement.EN_ATTENTE);

                for (Paiement p : overdue) {
                    p.setStatut(StatutPaiement.EN_RETARD);
                    paiementRepository.save(p);
                }

                log.info("Tenant {}: Overdue payments updated: {} payments marked as EN_RETARD", tenantId, overdue.size());
            } catch (Exception e) {
                log.error("Error checking overdue payments for tenant {}: {}", tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }
}
