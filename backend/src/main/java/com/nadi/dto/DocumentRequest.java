package com.nadi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentRequest {

    @NotNull(message = "Le joueur est obligatoire")
    private Long joueurId;

    @NotNull(message = "Le type est obligatoire")
    private String type;

    private LocalDate dateExpiration;
}
