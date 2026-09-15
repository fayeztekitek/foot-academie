package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementResponse {

    private Long id;
    private String plan;
    private String statut;
    private BigDecimal montantMensuel;
    private String devise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean renouvellementAuto;
    private Integer maxJoueurs;
    private Integer maxCoachs;
    private Integer maxParents;
    private Integer joueursUtilises;
    private Integer coachsUtilises;
    private Integer parentsUtilises;
    private Boolean joueursLimitReached;
    private Boolean coachsLimitReached;
    private Boolean parentsLimitReached;
}
