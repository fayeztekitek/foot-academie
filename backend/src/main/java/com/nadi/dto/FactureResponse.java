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
public class FactureResponse {

    private Long id;
    private String numero;
    private BigDecimal montant;
    private String devise;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private String statut;
    private String description;
    private String reference;
    private LocalDateTime createdAt;
}
