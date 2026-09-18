package com.nadi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class EntraineurRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String specialite;

    private String telephone;

    private String email;

    private String photoUrl;

    private Set<Long> categorieIds;
}
