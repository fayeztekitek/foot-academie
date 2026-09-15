package com.nadi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "academie")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Academie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String nom;

    private String logoUrl;

    private String adresse;

    private String ville;

    private String telephone;

    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private LocalDate dateActivation;

    private LocalDate dateExpiration;

    @Builder.Default
    private String plan = "FREE";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
