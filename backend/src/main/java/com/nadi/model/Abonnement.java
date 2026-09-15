package com.nadi.model;

import com.nadi.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonnement")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutAbonnement statut = StatutAbonnement.ACTIF;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantMensuel;

    private String devise;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Builder.Default
    private Boolean renouvellementAuto = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxJoueurs = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxCoachs = 5;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxParents = 100;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum StatutAbonnement {
        ACTIF, EXPIRE, SUSPENDU, ANNULE
    }
}
