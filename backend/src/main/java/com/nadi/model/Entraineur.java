package com.nadi.model;

import com.nadi.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "entraineur", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "tenant_id"})
})
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entraineur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    private String specialite;

    private String telephone;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "entraineur_categorie",
        joinColumns = @JoinColumn(name = "entraineur_id"),
        inverseJoinColumns = @JoinColumn(name = "categorie_id")
    )
    @Builder.Default
    private Set<Categorie> categories = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", unique = true)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "entraineur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Creneau> creneaux = new ArrayList<>();

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
