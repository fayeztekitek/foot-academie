package com.nadi.model;

import com.nadi.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_joueur")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteJoueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur joueur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entraineur_id", nullable = false)
    private Entraineur entraineur;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal physique;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal technique;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal explosivite;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal tactique;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal mental;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal endurance;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal noteGlobale;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculerNoteGlobale() {
        if (physique != null && technique != null && explosivite != null && tactique != null && mental != null && endurance != null) {
            this.noteGlobale = physique.add(technique).add(explosivite).add(tactique).add(mental).add(endurance)
                    .divide(BigDecimal.valueOf(6), 1, RoundingMode.HALF_UP);
        }
    }
}
