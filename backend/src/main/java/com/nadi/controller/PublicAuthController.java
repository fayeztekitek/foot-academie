package com.nadi.controller;

import com.nadi.dto.TenantPublicResponse;
import com.nadi.model.Academie;
import com.nadi.repository.AcademieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PublicAuthController {

    private final AcademieRepository academieRepository;

    @GetMapping("/tenants")
    public List<TenantPublicResponse> getActiveTenants() {
        return academieRepository.findAll().stream()
                .filter(Academie::getActive)
                .map(a -> TenantPublicResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom())
                        .slug(a.getSlug())
                        .ville(a.getVille())
                        .logoUrl(a.getLogoUrl())
                        .build())
                .toList();
    }
}
