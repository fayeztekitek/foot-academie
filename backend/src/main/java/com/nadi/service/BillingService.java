package com.nadi.service;

import com.nadi.config.BillingConfig;
import com.nadi.dto.AbonnementRequest;
import com.nadi.dto.AbonnementResponse;
import com.nadi.dto.FactureResponse;
import com.nadi.model.Abonnement;
import com.nadi.model.Facture;
import com.nadi.repository.AbonnementRepository;
import com.nadi.repository.FactureRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final AbonnementRepository abonnementRepository;
    private final FactureRepository factureRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;
    private final BillingConfig billingConfig;

    private static final AtomicInteger invoiceCounter = new AtomicInteger(1000);

    @Transactional(readOnly = true)
    public AbonnementResponse getCurrentAbonnement() {
        Long tenantId = TenantContext.getTenantId();
        Abonnement abonnement = abonnementRepository.findByTenantId(tenantId).orElse(null);

        if (abonnement == null) {
            return AbonnementResponse.builder()
                    .plan("FREE")
                    .statut("AUCUN")
                    .montantMensuel(BigDecimal.ZERO)
                    .devise("TND")
                    .maxJoueurs(10)
                    .maxCoachs(2)
                    .maxParents(20)
                    .joueursUtilises((int) joueurRepository.countByTenantId(tenantId))
                    .coachsUtilises((int) entraineurRepository.countByTenantId(tenantId))
                    .parentsUtilises((int) parentRepository.countByTenantId(tenantId))
                    .joueursLimitReached(joueurRepository.countByTenantId(tenantId) >= 10)
                    .coachsLimitReached(entraineurRepository.countByTenantId(tenantId) >= 2)
                    .parentsLimitReached(parentRepository.countByTenantId(tenantId) >= 20)
                    .build();
        }

        int joueursUtilises = (int) joueurRepository.countByTenantId(tenantId);
        int coachsUtilises = (int) entraineurRepository.countByTenantId(tenantId);
        int parentsUtilises = (int) parentRepository.countByTenantId(tenantId);

        return AbonnementResponse.builder()
                .id(abonnement.getId())
                .plan(abonnement.getPlan())
                .statut(abonnement.getStatut().name())
                .montantMensuel(abonnement.getMontantMensuel())
                .devise(abonnement.getDevise())
                .dateDebut(abonnement.getDateDebut())
                .dateFin(abonnement.getDateFin())
                .renouvellementAuto(abonnement.getRenouvellementAuto())
                .maxJoueurs(abonnement.getMaxJoueurs())
                .maxCoachs(abonnement.getMaxCoachs())
                .maxParents(abonnement.getMaxParents())
                .joueursUtilises(joueursUtilises)
                .coachsUtilises(coachsUtilises)
                .parentsUtilises(parentsUtilises)
                .joueursLimitReached(joueursUtilises >= abonnement.getMaxJoueurs())
                .coachsLimitReached(coachsUtilises >= abonnement.getMaxCoachs())
                .parentsLimitReached(parentsUtilises >= abonnement.getMaxParents())
                .build();
    }

    @Transactional
    public AbonnementResponse subscribe(AbonnementRequest request) {
        Long tenantId = TenantContext.getTenantId();

        BillingConfig.PlanLimits planLimits = billingConfig.getPlans().get(request.getPlan());
        if (planLimits == null) {
            throw new RuntimeException("Plan inconnu: " + request.getPlan());
        }

        Abonnement existing = abonnementRepository.findByTenantId(tenantId).orElse(null);
        if (existing != null && existing.getStatut() == Abonnement.StatutAbonnement.ACTIF) {
            existing.setPlan(request.getPlan());
            existing.setMontantMensuel(planLimits.getMontantMensuel());
            existing.setMaxJoueurs(planLimits.getMaxJoueurs());
            existing.setMaxCoachs(planLimits.getMaxCoachs());
            existing.setMaxParents(planLimits.getMaxParents());
            existing.setDateFin(LocalDate.now().plusMonths(1));
            abonnementRepository.save(existing);

            generateFacture(existing, "Upgrade vers " + request.getPlan());
            return getCurrentAbonnement();
        }

        Abonnement abonnement = Abonnement.builder()
                .plan(request.getPlan())
                .statut(Abonnement.StatutAbonnement.ACTIF)
                .montantMensuel(planLimits.getMontantMensuel())
                .devise(planLimits.getDevise() != null ? planLimits.getDevise() : "TND")
                .dateDebut(LocalDate.now())
                .dateFin(LocalDate.now().plusMonths(1))
                .renouvellementAuto(true)
                .maxJoueurs(planLimits.getMaxJoueurs())
                .maxCoachs(planLimits.getMaxCoachs())
                .maxParents(planLimits.getMaxParents())
                .tenantId(tenantId)
                .build();

        abonnementRepository.save(abonnement);
        generateFacture(abonnement, "Abonnement " + request.getPlan());
        return getCurrentAbonnement();
    }

    @Transactional
    public AbonnementResponse cancelAbonnement() {
        Long tenantId = TenantContext.getTenantId();
        Abonnement abonnement = abonnementRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement actif"));

        abonnement.setStatut(Abonnement.StatutAbonnement.ANNULE);
        abonnement.setRenouvellementAuto(false);
        abonnementRepository.save(abonnement);

        return getCurrentAbonnement();
    }

    @Transactional
    public void checkLimits(String resourceType) {
        Long tenantId = TenantContext.getTenantId();
        Abonnement abonnement = abonnementRepository.findByTenantId(tenantId).orElse(null);

        int max;
        long used;
        String label;

        switch (resourceType) {
            case "joueur":
                max = abonnement != null ? abonnement.getMaxJoueurs() : 10;
                used = joueurRepository.countByTenantId(tenantId);
                label = "joueurs";
                break;
            case "coach":
                max = abonnement != null ? abonnement.getMaxCoachs() : 2;
                used = entraineurRepository.countByTenantId(tenantId);
                label = "entraîneurs";
                break;
            case "parent":
                max = abonnement != null ? abonnement.getMaxParents() : 20;
                used = parentRepository.countByTenantId(tenantId);
                label = "parents";
                break;
            default:
                return;
        }

        if (used >= max) {
            throw new RuntimeException("Limite atteinte: " + label + " (" + used + "/" + max + "). Veuillez upgrader votre plan.");
        }
    }

    @Transactional(readOnly = true)
    public List<FactureResponse> getFactures() {
        Long tenantId = TenantContext.getTenantId();
        return factureRepository.findByTenantIdOrderByDateEmissionDesc(tenantId)
                .stream()
                .map(this::toFactureResponse)
                .collect(Collectors.toList());
    }

    private void generateFacture(Abonnement abonnement, String description) {
        String numero = "FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + invoiceCounter.incrementAndGet();

        Facture facture = Facture.builder()
                .numero(numero)
                .montant(abonnement.getMontantMensuel())
                .devise(abonnement.getDevise())
                .dateEmission(LocalDate.now())
                .dateEcheance(LocalDate.now().plusDays(30))
                .statut(Facture.StatutFacture.EN_ATTENTE)
                .description(description)
                .abonnement(abonnement)
                .tenantId(abonnement.getTenantId())
                .build();

        factureRepository.save(facture);
    }

    private FactureResponse toFactureResponse(Facture facture) {
        return FactureResponse.builder()
                .id(facture.getId())
                .numero(facture.getNumero())
                .montant(facture.getMontant())
                .devise(facture.getDevise())
                .dateEmission(facture.getDateEmission())
                .dateEcheance(facture.getDateEcheance())
                .datePaiement(facture.getDatePaiement())
                .statut(facture.getStatut().name())
                .description(facture.getDescription())
                .reference(facture.getReference())
                .createdAt(facture.getCreatedAt())
                .build();
    }
}
