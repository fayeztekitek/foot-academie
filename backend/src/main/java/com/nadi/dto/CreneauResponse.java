package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<EntraineurInfo> entraineurs;
    private String terrain;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntraineurInfo {
        private Long id;
        private String nom;
        private String prenom;
    }
}
