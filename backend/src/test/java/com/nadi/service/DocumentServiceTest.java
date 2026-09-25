package com.nadi.service;

import com.nadi.dto.DocumentResponse;
import com.nadi.model.*;
import com.nadi.repository.DocumentRepository;
import com.nadi.repository.JoueurRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.repository.UtilisateurRepository;
import com.nadi.security.FamilyAccessGuard;
import com.nadi.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private JoueurRepository joueurRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        FamilyAccessGuard guard = new FamilyAccessGuard(
                parentRepository, new SecurityUtils(utilisateurRepository));
        documentService = new DocumentService(documentRepository, joueurRepository, guard);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateParent(Long userId) {
        Utilisateur user = Utilisateur.builder()
                .id(userId).email("p@nadi.tn").motDePasseHash("hash")
                .role(Role.PARENT).actif(true).tenantId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_PARENT"))));
    }

    private Joueur joueur(Long parentId) {
        Joueur joueur = Joueur.builder().prenom("J").nom("N")
                .dateNaissance(LocalDate.of(2015, 1, 1)).build();
        joueur.setId(1L);
        Parent parent = Parent.builder().id(parentId).prenom("P").nom("N").email("p@nadi.tn").build();
        joueur.setParent(parent);
        return joueur;
    }

    private Document document(Joueur joueur) {
        return Document.builder().id(1L).joueur(joueur)
                .type(TypeDocument.CERTIFICAT_MEDICAL).statut(StatutDocument.VALIDE)
                .fichierUrl("uploads/documents/f.pdf").nomFichier("f.pdf").build();
    }

    @Test
    void parentCanListOwnChildDocuments() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(documentRepository.findByJoueurId(1L)).thenReturn(List.of(document(joueur(10L))));

        List<DocumentResponse> responses = documentService.getByJoueur(1L);

        assertEquals(1, responses.size());
        assertEquals("CERTIFICAT_MEDICAL", responses.get(0).getType());
    }

    @Test
    void parentCannotListOtherChildDocuments() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(11L)));

        assertThrows(AccessDeniedException.class, () -> documentService.getByJoueur(1L));
    }

    @Test
    void parentCanListOwnConsentsByParentId() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));
        when(documentRepository.findByParentId(10L)).thenReturn(List.of());

        assertTrue(documentService.getByParent(10L).isEmpty());
    }

    @Test
    void parentCannotListOtherParentDocuments() {
        authenticateParent(42L);
        when(parentRepository.findByUtilisateurId(42L))
                .thenReturn(Optional.of(Parent.builder().id(10L).prenom("P").nom("N").email("p@nadi.tn").build()));

        assertThrows(AccessDeniedException.class, () -> documentService.getByParent(11L));
    }

    private MultipartFile file(String name, boolean empty) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(empty);
        lenient().when(file.getOriginalFilename()).thenReturn(name);
        lenient().when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        return file;
    }

    @Test
    void uploadRejectsPathTraversalFilename() throws Exception {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));

        assertThrows(RuntimeException.class, () ->
                documentService.upload(1L, "CERTIFICAT_MEDICAL", file("../../evil.pdf", false)));
    }

    @Test
    void uploadRejectsDisallowedExtension() throws Exception {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));

        assertThrows(RuntimeException.class, () ->
                documentService.upload(1L, "CERTIFICAT_MEDICAL", file("evil.html", false)));
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));

        assertThrows(RuntimeException.class, () ->
                documentService.upload(1L, "CERTIFICAT_MEDICAL", file("doc.pdf", true)));
    }

    @Test
    void uploadAcceptsValidPdf() throws Exception {
        when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur(10L)));
        when(documentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        DocumentResponse response = documentService.upload(1L, "CERTIFICAT_MEDICAL", file("certificat.pdf", false));

        assertEquals("CERTIFICAT_MEDICAL", response.getType());
        assertTrue(response.getFichierUrl().endsWith(".pdf"));
        verify(documentRepository).save(any());
    }
}
