package com.nadi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "billing")
@Data
public class BillingConfig {

    private Map<String, PlanLimits> plans = new HashMap<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanLimits {
        private int maxJoueurs;
        private int maxCoachs;
        private int maxParents;
        private BigDecimal montantMensuel;
        private String devise;
    }
}
