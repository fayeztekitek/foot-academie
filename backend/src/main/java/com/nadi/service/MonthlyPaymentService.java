package com.nadi.service;

import com.nadi.model.GenerationMensuelle;
import com.nadi.model.Joueur;
import com.nadi.model.Paiement;
import com.nadi.model.StatutPaiement;
import com.nadi.repository.GenerationMensuelleRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Monthly fee generation that cannot be silently missed.
 *
 * The midnight cron only fires while the JVM is awake — on sleeping hosts
 * the 1st-of-month run is lost with no recovery. Every generation is
 * therefore recorded per tenant+month, and a boot catch-up regenerates
 * any missing month (current + previous). Re-running is always safe:
 * players with a non-cancelled payment for the month are skipped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyPaymentService {

    private static final BigDecimal MONTANT_MENSUEL = new BigDecimal("60.000");
    private static final DateTimeFormatter MOIS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JoueurRepository joueurRepository;
    private final PaiementRepository paiementRepository;
    private final GenerationMensuelleRepository generationRepository;

    @Transactional
    public int ensureMonthlyPayments(Long tenantId, YearMonth month) {
        String mois = month.format(MOIS_FORMAT);
        if (generationRepository.existsByTenantIdAndMois(tenantId, mois)) {
            return 0;
        }

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        int created = 0;
        int skippedNoParent = 0;
        int skippedNoEntry = 0;
        int skippedFuture = 0;
        int skippedNoFrequency = 0;
        int skippedExisting = 0;

        for (Joueur joueur : joueurRepository.findAll()) {
            if (joueur.getParent() == null) {
                skippedNoParent++;
                continue;
            }
            if (joueur.getDateEntree() == null) {
                skippedNoEntry++;
                continue;
            }
            if (joueur.getDateEntree().isAfter(monthEnd)) {
                skippedFuture++;
                continue;
            }
            if (joueur.getFrequence() == null) {
                skippedNoFrequency++;
                continue;
            }

            boolean alreadyExists = paiementRepository
                    .findByJoueurIdAndDateEcheanceBetween(joueur.getId(), monthStart, monthEnd)
                    .stream().anyMatch(p -> p.getStatut() != StatutPaiement.ANNULE);
            if (alreadyExists) {
                skippedExisting++;
                continue;
            }

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

        try {
            generationRepository.save(GenerationMensuelle.builder()
                    .tenantId(tenantId)
                    .mois(mois)
                    .build());
        } catch (DataIntegrityViolationException e) {
            log.info("Tenant {}: monthly generation for {} raced, keeping existing result", tenantId, mois);
        }

        log.info("Tenant {}: monthly payments for {}: {} created "
                        + "(skipped: no-parent={}, no-entry-date={}, future-entry={}, no-frequency={}, existing={})",
                tenantId, mois, created,
                skippedNoParent, skippedNoEntry, skippedFuture, skippedNoFrequency, skippedExisting);
        return created;
    }
}
