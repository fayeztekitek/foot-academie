package com.nadi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategorieRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "L'âge minimum est obligatoire")
    @Min(value = 0, message = "L'âge minimum doit être positif")
    private Integer ageMin;

    @NotNull(message = "L'âge maximum est obligatoire")
    @Min(value = 0, message = "L'âge maximum doit être positif")
    private Integer ageMax;
}
