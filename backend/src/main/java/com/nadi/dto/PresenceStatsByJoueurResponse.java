package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceStatsByJoueurResponse {
    private Long joueurId;
    private String joueurPrenom;
    private String joueurNom;
    private PresenceStatsResponse mois;
    private PresenceStatsResponse trimestre;
    private PresenceStatsResponse semestre;
    private PresenceStatsResponse annee;
    private PresenceStatsResponse allTime;
}
