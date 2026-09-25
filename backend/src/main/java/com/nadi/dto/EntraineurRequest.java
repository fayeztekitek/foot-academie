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

    @jakarta.validation.constraints.Pattern(
            regexp = "^(https?://|data:image/(png|jpeg|gif|webp);base64,).{0,2000000}$",
            message = "URL de photo invalide (http(s) ou image base64 uniquement)")
    private String photoUrl;

    private String motDePasse;

    private Set<Long> categorieIds;
}
