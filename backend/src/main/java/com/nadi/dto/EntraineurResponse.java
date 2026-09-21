package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntraineurResponse {

    private Long id;
    private String prenom;
    private String nom;
    private String specialite;
    private String telephone;
    private String email;
    private String photoUrl;
    private List<CategorieResponse> categories;
    private Long utilisateurId;
    private Boolean mustChangePassword;
    private LocalDateTime createdAt;
}
