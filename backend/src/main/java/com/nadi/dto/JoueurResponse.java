package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoueurResponse {

    private Long id;
    private String prenom;
    private String nom;
    private LocalDate dateNaissance;
    private LocalDate dateEntree;
    private Long categorieId;
    private String categorieNom;
    private Long parentId;
    private String parentNom;
    private String parentPrenom;
    private String statutPaiement;
    private String frequence;
    private String photoUrl;
    private Boolean certificatMedical;
    private Boolean autorisationParentale;
    private Integer moisAVerser;
    private Integer moisPayes;
    private Integer moisImpayes;
    private LocalDateTime createdAt;
}
