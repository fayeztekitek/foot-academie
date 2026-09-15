package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponse {

    private Long academieId;
    private String academieSlug;
    private String academieNom;
    private String adminEmail;
    private String message;
}
