package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentLogResponse {
    private Long id;
    private Long parentId;
    private String parentNom;
    private String parentPrenom;
    private String parentEmail;
    private String type;
    private boolean granted;
    private String details;
    private String ipAddress;
    private boolean exported;
    private LocalDateTime dateConsentement;
}
