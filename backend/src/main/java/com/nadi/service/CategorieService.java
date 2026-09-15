package com.nadi.service;

import com.nadi.dto.CategorieRequest;
import com.nadi.dto.CategorieResponse;
import com.nadi.model.Categorie;
import com.nadi.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    @Transactional(readOnly = true)
    public Page<CategorieResponse> getAll(Pageable pageable) {
        return categorieRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CategorieResponse> search(String nom, Pageable pageable) {
        return categorieRepository.findByNomContainingIgnoreCase(nom, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategorieResponse getById(Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + id));
        return toResponse(categorie);
    }

    @Transactional
    public CategorieResponse create(CategorieRequest request) {
        Categorie categorie = Categorie.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .ageMin(request.getAgeMin())
                .ageMax(request.getAgeMax())
                .build();
        return toResponse(categorieRepository.save(categorie));
    }

    @Transactional
    public CategorieResponse update(Long id, CategorieRequest request) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée: " + id));
        categorie.setNom(request.getNom());
        categorie.setDescription(request.getDescription());
        categorie.setAgeMin(request.getAgeMin());
        categorie.setAgeMax(request.getAgeMax());
        return toResponse(categorieRepository.save(categorie));
    }

    @Transactional
    public void delete(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée: " + id);
        }
        categorieRepository.deleteById(id);
    }

    private CategorieResponse toResponse(Categorie c) {
        return CategorieResponse.builder()
                .id(c.getId())
                .nom(c.getNom())
                .description(c.getDescription())
                .ageMin(c.getAgeMin())
                .ageMax(c.getAgeMax())
                .joueurCount(c.getJoueurs() != null ? c.getJoueurs().size() : 0)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
