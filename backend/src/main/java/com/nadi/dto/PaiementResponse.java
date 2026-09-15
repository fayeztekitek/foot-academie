package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementResponse {

    private Long id;
    private Long joueurId;
    private String joueurNom;
    private String joueurPrenom;
    private Long parentId;
    private String parentNom;
    private String parentPrenom;
    private BigDecimal montant;
    private String devise;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private LocalDate dateEncaissement;
    private String statut;
    private String moyenPaiement;
    private String formule;
    private String frequence;
    private String reference;
    private String numeroRecu;
    private String commentaire;
    private String notes;
    private Long enregistreParId;
    private String enregistreParEmail;
    private LocalDateTime createdAt;
}
