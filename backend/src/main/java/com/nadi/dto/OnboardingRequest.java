package com.nadi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingRequest {

    @NotBlank(message = "Le nom de l'académie est obligatoire")
    private String academieNom;

    private String academieVille;

    private String academieAdresse;

    private String academieTelephone;

    @NotBlank(message = "L'email de l'admin est obligatoire")
    @Email(message = "Email invalide")
    private String adminEmail;

    @NotBlank(message = "Le prénom de l'admin est obligatoire")
    private String adminPrenom;

    @NotBlank(message = "Le nom de l'admin est obligatoire")
    private String adminNom;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String adminMotDePasse;
}
