package com.nadi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class NoteJoueurRequest {

    @NotNull(message = "Le joueur est obligatoire")
    private Long joueurId;

    @NotNull(message = "Le créneau est obligatoire")
    private Long creneauId;

    @NotNull(message = "L'entraîneur est obligatoire")
    private Long entraineurId;

    private LocalDate date;

    @NotNull(message = "La note physique est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal physique;

    @NotNull(message = "La note technique est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal technique;

    @NotNull(message = "La note d'explosivité est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal explosivite;

    @NotNull(message = "La note tactique est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal tactique;

    @NotNull(message = "La note mentale est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal mental;

    @NotNull(message = "La note d'endurance est obligatoire")
    @Min(0) @Max(10)
    private BigDecimal endurance;
}
