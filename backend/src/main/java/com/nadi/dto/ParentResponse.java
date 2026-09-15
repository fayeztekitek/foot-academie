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
public class ParentResponse {

    private Long id;
    private String prenom;
    private String nom;
    private String telephone;
    private String email;
    private Boolean consentementRGPD;
    private LocalDate dateConsentementRGPD;
    private Long utilisateurId;
    private String motDePasse;
    private List<JoueurResponse> joueurs;
    private LocalDateTime createdAt;
}
