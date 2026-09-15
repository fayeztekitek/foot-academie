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
public class AbsenceResponse {

    private Long id;
    private Long joueurId;
    private String joueurPrenom;
    private String joueurNom;
    private Long creneauId;
    private String categorieNom;
    private LocalDate dateSeance;
    private Boolean present;
    private String marqueParEmail;
    private LocalDateTime createdAt;
}
