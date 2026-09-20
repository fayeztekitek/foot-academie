package com.nadi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JoueurRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;

    private LocalDate dateEntree;

    private Long categorieId;

    private Long parentId;

    private String frequence;

    private String photoUrl;

    private String postePrincipal;

    private String postesSecondaires;

    private Integer taille;

    private Integer poids;

    private Boolean certificatMedical;

    private Boolean autorisationParentale;
}
