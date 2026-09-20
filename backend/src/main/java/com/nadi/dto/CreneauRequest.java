package com.nadi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

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

    private Long entraineurId;

    private List<Long> entraineurIds;

    @NotBlank(message = "Le terrain est obligatoire")
    private String terrain;
}
