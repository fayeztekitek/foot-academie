package com.nadi.config;

import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import com.nadi.service.MonthlyPaymentService;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * Boot catch-up for monthly fee generation. The 1st-of-month cron only
 * fires while the JVM is awake — on hosts that sleep, the run is silently
 * missed. On every boot, regenerate the current month and the previous
 * one when they were never generated. Fully idempotent: months already
 * generated (or players already billed) are skipped.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyPaymentCatchUp implements ApplicationRunner {

    private final AcademieRepository academieRepository;
    private final MonthlyPaymentService monthlyPaymentService;

    @Override
    public void run(ApplicationArguments args) {
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);
        for (Academie academie : academieRepository.findAll()) {
            Long tenantId = academie.getId();
            try {
                TenantContext.setTenantId(tenantId);
                int currentCreated = monthlyPaymentService.ensureMonthlyPayments(tenantId, current);
                int previousCreated = monthlyPaymentService.ensureMonthlyPayments(tenantId, previous);
                if (currentCreated > 0 || previousCreated > 0) {
                    log.info("Tenant {}: boot catch-up generated {} + {} payments for {}/{}",
                            tenantId, previousCreated, currentCreated, previous, current);
                }
            } catch (Exception e) {
                log.error("Tenant {}: boot catch-up failed: {}", tenantId, e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }
}
