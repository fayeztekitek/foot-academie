package com.nadi.service;

import com.nadi.dto.DocumentRequest;
import com.nadi.dto.DocumentResponse;
import com.nadi.model.*;
import com.nadi.repository.DocumentRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.security.FamilyAccessGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final JoueurRepository joueurRepository;
    private final FamilyAccessGuard familyAccessGuard;

    private static final String UPLOAD_DIR = "uploads/documents/";
    private static final java.util.Set<String> ALLOWED_EXTENSIONS =
            java.util.Set.of("pdf", "jpg", "jpeg", "png");

    @Transactional(readOnly = true)
    public List<DocumentResponse> getByJoueur(Long joueurId) {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));
        familyAccessGuard.requireAccessToJoueur(joueur);
        return documentRepository.findByJoueurId(joueurId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getByParent(Long parentId) {
        familyAccessGuard.requireOwnParentId(parentId);
        return documentRepository.findByParentId(parentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getExpiringWithinDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);
        return documentRepository.findExpiringBetween(today, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getExpired() {
        return documentRepository.findExpiredNotMarked(LocalDate.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        Joueur joueur = joueurRepository.findById(request.getJoueurId())
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + request.getJoueurId()));

        Document doc = Document.builder()
                .joueur(joueur)
                .type(TypeDocument.valueOf(request.getType()))
                .dateExpiration(request.getDateExpiration())
                .statut(request.getDateExpiration() != null && request.getDateExpiration().isBefore(LocalDate.now())
                        ? StatutDocument.EXPIRE : StatutDocument.VALIDE)
                .build();
        return toResponse(documentRepository.save(doc));
    }

    @Transactional
    public DocumentResponse upload(Long joueurId, String type, MultipartFile file) throws IOException {
        Joueur joueur = joueurRepository.findById(joueurId)
                .orElseThrow(() -> new RuntimeException("Joueur non trouvé: " + joueurId));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        }
        String original = org.springframework.util.StringUtils.cleanPath(
                java.util.Objects.requireNonNull(file.getOriginalFilename(), "Nom de fichier manquant"));
        if (original.contains("..") || original.contains("/") || original.contains("\\")) {
            throw new RuntimeException("Nom de fichier invalide");
        }
        String ext = org.springframework.util.StringUtils.getFilenameExtension(original);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new RuntimeException("Type de fichier non autorisé (pdf, jpg, jpeg, png uniquement)");
        }

        String filename = UUID.randomUUID() + "." + ext.toLowerCase();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        Document doc = Document.builder()
                .joueur(joueur)
                .type(TypeDocument.valueOf(type))
                .fichierUrl(UPLOAD_DIR + filename)
                .nomFichier(file.getOriginalFilename())
                .statut(StatutDocument.VALIDE)
                .build();
        return toResponse(documentRepository.save(doc));
    }

    @Transactional
    public DocumentResponse updateStatut(Long id, StatutDocument statut) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé: " + id));
        doc.setStatut(statut);
        return toResponse(documentRepository.save(doc));
    }

    @Transactional
    public void updateExpiryStatuses() {
        LocalDate today = LocalDate.now();
        List<Document> expired = documentRepository.findExpiredNotMarked(today);
        for (Document doc : expired) {
            doc.setStatut(StatutDocument.EXPIRE);
            documentRepository.save(doc);
        }
    }

    private DocumentResponse toResponse(Document d) {
        long joursRestants = d.getDateExpiration() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), d.getDateExpiration())
                : -1;

        return DocumentResponse.builder()
                .id(d.getId())
                .joueurId(d.getJoueur().getId())
                .joueurNom(d.getJoueur().getNom())
                .joueurPrenom(d.getJoueur().getPrenom())
                .type(d.getType().name())
                .fichierUrl(d.getFichierUrl())
                .nomFichier(d.getNomFichier())
                .dateExpiration(d.getDateExpiration())
                .statut(d.getStatut().name())
                .joursRestants((int) joursRestants)
                .createdAt(d.getCreatedAt())
                .build();
    }
}
