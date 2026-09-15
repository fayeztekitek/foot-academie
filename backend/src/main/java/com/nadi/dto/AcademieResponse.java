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
public class AcademieResponse {

    private Long id;
    private String slug;
    private String nom;
    private String logoUrl;
    private String adresse;
    private String ville;
    private String telephone;
    private String email;
    private Boolean active;
    private LocalDate dateActivation;
    private LocalDate dateExpiration;
    private String plan;
    private Long joueurCount;
    private Long parentCount;
    private Long coachCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
