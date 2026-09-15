package com.nadi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AbsenceRequest {

    @NotNull(message = "Le créneau est obligatoire")
    private Long creneauId;

    @NotNull(message = "La date de séance est obligatoire")
    private LocalDate dateSeance;

    private List<PresenceItem> presences;

    @Data
    public static class PresenceItem {
        @NotNull
        private Long joueurId;
        @NotNull
        private Boolean present;
    }
}
