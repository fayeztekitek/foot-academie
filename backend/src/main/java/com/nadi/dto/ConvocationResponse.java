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
public class ConvocationResponse {

    private Long id;
    private Long evenementId;
    private String evenementTitre;
    private String evenementType;
    private LocalDate evenementDateDebut;
    private String evenementLieu;
    private Long joueurId;
    private String joueurPrenom;
    private String joueurNom;
    private Long parentId;
    private String parentPrenom;
    private String parentNom;
    private String parentEmail;
    private String statut;
    private LocalDateTime dateReponse;
    private LocalDateTime createdAt;
}
