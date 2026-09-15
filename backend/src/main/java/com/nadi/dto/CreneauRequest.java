package com.nadi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreneauRequest {

    @NotNull(message = "Le jour de la semaine est obligatoire")
    private String jourSemaine;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    @NotNull(message = "L'entraîneur est obligatoire")
    private Long entraineurId;

    @NotBlank(message = "Le terrain est obligatoire")
    private String terrain;
}
