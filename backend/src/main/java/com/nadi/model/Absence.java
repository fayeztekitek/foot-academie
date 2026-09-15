package com.nadi.model;

import com.nadi.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "absence", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"joueur_id", "creneau_id", "date_seance"})
})
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;

    @Column(name = "date_seance", nullable = false)
    private LocalDate dateSeance;

    @Column(nullable = false)
    @Builder.Default
    private Boolean present = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marque_par_id")
    private Utilisateur marquePar;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
