package com.nadi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantPublicResponse {
    private Long id;
    private String nom;
    private String slug;
    private String ville;
    private String logoUrl;
}
