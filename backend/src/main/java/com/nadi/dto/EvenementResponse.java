package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenementResponse {

    private Long id;
    private String titre;
    private String description;
    private String typeEvenement;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String heureDebut;
    private String heureFin;
    private String lieu;
    private String terrain;
    private Long creeParId;
    private String creeParNom;
    private int nbConvocations;
    private int nbConfirmes;
    private LocalDateTime createdAt;
}
