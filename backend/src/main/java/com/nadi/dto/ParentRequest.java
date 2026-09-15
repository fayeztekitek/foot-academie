package com.nadi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParentRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private String motDePasse;

    private Boolean consentementRGPD;
}
