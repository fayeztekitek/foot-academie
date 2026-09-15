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
public class CategorieResponse {

    private Long id;
    private String nom;
    private String description;
    private Integer ageMin;
    private Integer ageMax;
    private Integer joueurCount;
    private LocalDateTime createdAt;
}
