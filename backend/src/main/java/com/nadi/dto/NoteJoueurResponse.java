package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteJoueurResponse {

    private Long id;
    private Long joueurId;
    private String joueurNom;
    private String joueurPrenom;
    private Long creneauId;
    private String creneauDescription;
    private Long entraineurId;
    private String entraineurNom;
    private LocalDate date;
    private BigDecimal physique;
    private BigDecimal technique;
    private BigDecimal explosivite;
    private BigDecimal tactique;
    private BigDecimal mental;
    private BigDecimal endurance;
    private BigDecimal noteGlobale;
    private LocalDateTime createdAt;
}
