package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreneauResponse {

    private Long id;
    private String jourSemaine;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Long categorieId;
    private String categorieNom;
    private Long entraineurId;
    private String entraineurNom;
    private String entraineurPrenom;
    private String terrain;
    private LocalDateTime createdAt;
}
