package com.nadi.model;

import com.nadi.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "generation_mensuelle", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "mois"})
})
@EntityListeners(TenantEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationMensuelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private String mois;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime executedAt;
}
