package com.nadi.service;

import com.nadi.dto.AcademieRequest;
import com.nadi.dto.AcademieResponse;
import com.nadi.model.Academie;
import com.nadi.model.Utilisateur;
import com.nadi.repository.AcademieRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.EntraineurRepository;
import com.nadi.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AcademieService {

    private final AcademieRepository academieRepository;
    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final EntraineurRepository entraineurRepository;

    @Transactional(readOnly = true)
    public Page<AcademieResponse> listAll(Pageable pageable) {
        return academieRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AcademieResponse getById(Long id) {
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));
        return toResponse(academie);
    }

    @Transactional
    public AcademieResponse create(AcademieRequest request) {
        if (academieRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Ce slug est déjà utilisé: " + request.getSlug());
        }

        Academie academie = Academie.builder()
                .slug(request.getSlug())
                .nom(request.getNom())
                .logoUrl(request.getLogoUrl())
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .active(request.getActive() != null ? request.getActive() : true)
                .dateActivation(request.getDateActivation() != null ? request.getDateActivation() : LocalDate.now())
                .dateExpiration(request.getDateExpiration())
                .plan(request.getPlan() != null ? request.getPlan() : "FREE")
                .build();

        return toResponse(academieRepository.save(academie));
    }

    @Transactional
    public AcademieResponse update(Long id, AcademieRequest request) {
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        if (request.getNom() != null) academie.setNom(request.getNom());
        if (request.getLogoUrl() != null) academie.setLogoUrl(request.getLogoUrl());
        if (request.getAdresse() != null) academie.setAdresse(request.getAdresse());
        if (request.getVille() != null) academie.setVille(request.getVille());
        if (request.getTelephone() != null) academie.setTelephone(request.getTelephone());
        if (request.getEmail() != null) academie.setEmail(request.getEmail());
        if (request.getActive() != null) academie.setActive(request.getActive());
        if (request.getDateActivation() != null) academie.setDateActivation(request.getDateActivation());
        if (request.getDateExpiration() != null) academie.setDateExpiration(request.getDateExpiration());
        if (request.getPlan() != null) academie.setPlan(request.getPlan());

        return toResponse(academieRepository.save(academie));
    }

    @Transactional
    public void delete(Long id) {
        if (!academieRepository.existsById(id)) {
            throw new RuntimeException("Académie non trouvée avec l'id: " + id);
        }
        academieRepository.deleteById(id);
    }

    @Transactional
    public AcademieResponse toggleActive(Long id) {
        Academie academie = academieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Académie non trouvée avec l'id: " + id));

        academie.setActive(!academie.getActive());
        if (academie.getActive() && academie.getDateActivation() == null) {
            academie.setDateActivation(LocalDate.now());
        }

        return toResponse(academieRepository.save(academie));
    }

    @Transactional(readOnly = true)
    public AcademieResponse getCurrentTenantInfo() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Aucun contexte tenant disponible");
        }
        return getById(tenantId);
    }

    private AcademieResponse toResponse(Academie academie) {
        Long joueurCount = 0L;
        Long parentCount = 0L;
        Long coachCount = 0L;

        try {
            joueurCount = joueurRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        try {
            parentCount = parentRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        try {
            coachCount = entraineurRepository.countByTenantId(academie.getId());
        } catch (Exception ignored) {}

        return AcademieResponse.builder()
                .id(academie.getId())
                .slug(academie.getSlug())
                .nom(academie.getNom())
                .logoUrl(academie.getLogoUrl())
                .adresse(academie.getAdresse())
                .ville(academie.getVille())
                .telephone(academie.getTelephone())
                .email(academie.getEmail())
                .active(academie.getActive())
                .dateActivation(academie.getDateActivation())
                .dateExpiration(academie.getDateExpiration())
                .plan(academie.getPlan())
                .joueurCount(joueurCount)
                .parentCount(parentCount)
                .coachCount(coachCount)
                .createdAt(academie.getCreatedAt())
                .updatedAt(academie.getUpdatedAt())
                .build();
    }
}
