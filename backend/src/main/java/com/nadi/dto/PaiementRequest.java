package com.nadi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaiementRequest {

    @NotNull(message = "Le joueur est obligatoire")
    private Long joueurId;

    @NotNull(message = "Le parent est obligatoire")
    private Long parentId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private String devise = "TND";

    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDate dateEcheance;

    private String moyenPaiement;

    private String statut;

    private String formule;

    private LocalDate dateEncaissement;

    private String numeroRecu;

    private String commentaire;

    private String notes;
}
