package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopJoueurResponse {

    private Long joueurId;
    private String prenom;
    private String nom;
    private String categorieNom;
    private BigDecimal moyenneNote;
    private Double tauxPresence;
    private Double score;
    private String periode;
}
