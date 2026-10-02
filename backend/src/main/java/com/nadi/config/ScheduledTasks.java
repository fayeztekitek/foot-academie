package com.nadi.config;

import com.nadi.model.*;
import com.nadi.repository.*;
import com.nadi.service.DocumentService;
import com.nadi.service.MonthlyPaymentService;
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
    private final PaiementRepository paiementRepository;
    private final AcademieRepository academieRepository;
    private final MonthlyPaymentService monthlyPaymentService;

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

        for (Long tenantId : allTenantIds()) {
            try {
                TenantContext.setTenantId(tenantId);
                int created = monthlyPaymentService.ensureMonthlyPayments(tenantId, currentMonth);
                log.info("Tenant {}: Monthly payments generated: {} new payments for {}",
                        tenantId, created, currentMonth);
            } catch (Exception e) {
                log.error("Error generating monthly payments for tenant {}: {}", tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }

    private List<Long> allTenantIds() {
        return academieRepository.findAll().stream()
                .map(Academie::getId)
                .toList();
    }

    /**
     * Daily reminder, one notification per parent account: total pending
     * amount with a per-child breakdown (month, amount, due date), so the
     * message is immediately actionable.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyPendingPayments() {
        log.info("Notifying parents of pending payments...");
        LocalDate today = LocalDate.now();

        List<Long> allTenantIds = academieRepository.findAll().stream()
                .map(Academie::getId)
                .toList();

        for (Long tenantId : allTenantIds) {
            try {
                TenantContext.setTenantId(tenantId);
                java.util.Map<Long, List<Paiement>> byUser = new java.util.LinkedHashMap<>();
                java.util.Map<Long, Utilisateur> usersById = new java.util.LinkedHashMap<>();
                for (Paiement paiement : paiementRepository.findPendingWithDetails()) {
                    if (paiement.getDateEcheance() != null && paiement.getDateEcheance().isAfter(today)) {
                        continue;
                    }
                    if (paiement.getParent() == null || paiement.getParent().getUtilisateur() == null) {
                        continue;
                    }
                    Long userId = paiement.getParent().getUtilisateur().getId();
                    usersById.putIfAbsent(userId, paiement.getParent().getUtilisateur());
                    byUser.computeIfAbsent(userId, k -> new java.util.ArrayList<>()).add(paiement);
                }

                for (java.util.Map.Entry<Long, List<Paiement>> entry : byUser.entrySet()) {
                    List<Paiement> pendings = entry.getValue();
                    BigDecimal total = pendings.stream()
                            .map(Paiement::getMontant)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (total.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    String details = pendings.stream()
                            .map(p -> {
                                String month = p.getDateEcheance() != null
                                        ? YearMonth.from(p.getDateEcheance()).getMonth()
                                                .getDisplayName(java.time.format.TextStyle.FULL,
                                                        java.util.Locale.FRENCH)
                                                + " " + YearMonth.from(p.getDateEcheance()).getYear()
                                        : "sans échéance";
                                return p.getJoueur().getPrenom() + " " + p.getJoueur().getNom()
                                        + " — " + month + " : " + p.getMontant() + " " + p.getDevise()
                                        + " (échéance " + p.getDateEcheance() + ")";
                            })
                            .collect(java.util.stream.Collectors.joining("\n"));
                    notificationService.create(usersById.get(entry.getKey()),
                            Notification.TypeNotification.PAIEMENT_RAPPEL,
                            "Mensualité en attente : " + total + " TND",
                            details + "\nMerci de régulariser auprès de l'administration.");
                }

                log.info("Tenant {}: Pending payment reminders sent to {} parents.", tenantId, byUser.size());
            } catch (Exception e) {
                log.error("Error notifying pending payments for tenant {}: {}", tenantId, e.getMessage());
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
