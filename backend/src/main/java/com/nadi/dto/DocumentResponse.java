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
public class DocumentResponse {

    private Long id;
    private Long joueurId;
    private String joueurNom;
    private String joueurPrenom;
    private String type;
    private String fichierUrl;
    private String nomFichier;
    private LocalDate dateExpiration;
    private String statut;
    private int joursRestants;
    private LocalDateTime createdAt;
}
