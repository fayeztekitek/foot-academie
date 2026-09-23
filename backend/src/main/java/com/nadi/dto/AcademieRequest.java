package com.nadi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademieRequest {

    @NotBlank(message = "Le slug est obligatoire")
    private String slug;

    @NotBlank(message = "Le nom est obligatoire")
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

    private String adminEmail;

    private String adminMotDePasse;
}
