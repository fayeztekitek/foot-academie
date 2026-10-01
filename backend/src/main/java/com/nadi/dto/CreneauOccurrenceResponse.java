package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauOccurrenceResponse {

    private Long creneauId;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String terrain;
    private Long categorieId;
    private String categorieNom;
    private String coachNom;
    private String statut;
    private String motif;
}
