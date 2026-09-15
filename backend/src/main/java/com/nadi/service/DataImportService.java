package com.nadi.service;

import com.nadi.model.*;
import com.nadi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {

    private final JoueurRepository joueurRepository;
    private final ParentRepository parentRepository;
    private final CategorieRepository categorieRepository;
    private final PaiementRepository paiementRepository;
    private final EntraineurRepository entraineurRepository;

    public static class ImportResult {
        public int joueursImported;
        public int parentsImported;
        public int paiementsImported;
        public int erreurs;
        public List<String> messages = new ArrayList<>();
    }

    @Transactional
    public ImportResult importPlayersCsv(InputStream inputStream) throws IOException {
        ImportResult result = new ImportResult();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String header = reader.readLine(); // skip header
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 4) {
                        result.messages.add("Ligne " + lineNumber + ": colonnes manquantes");
                        result.erreurs++;
                        continue;
                    }

                    String prenom = parts[0].trim();
                    String nom = parts[1].trim();
                    String dateNaissanceStr = parts[2].trim();
                    String categorieNom = parts[3].trim();
                    String parentEmail = parts.length > 4 ? parts[4].trim() : "";

                    LocalDate dateNaissance = LocalDate.parse(dateNaissanceStr, fmt);

                    Categorie categorie = null;
                    if (!categorieNom.isEmpty()) {
                        categorie = categorieRepository.findByNomContainingIgnoreCase(categorieNom)
                                .stream().findFirst().orElse(null);
                        if (categorie == null) {
                            categorie = Categorie.builder()
                                    .nom(categorieNom)
                                    .ageMin(0).ageMax(18)
                                    .build();
                            categorie = categorieRepository.save(categorie);
                        }
                    }

                    Parent parent = null;
                    if (!parentEmail.isEmpty()) {
                        parent = parentRepository.findByEmail(parentEmail)
                                .stream().findFirst().orElse(null);
                        if (parent == null) {
                            parent = Parent.builder()
                                    .prenom(prenom)
                                    .nom(nom + " (parent)")
                                    .email(parentEmail)
                                    .build();
                            parent = parentRepository.save(parent);
                            result.parentsImported++;
                        }
                    }

                    Joueur joueur = Joueur.builder()
                            .prenom(prenom)
                            .nom(nom)
                            .dateNaissance(dateNaissance)
                            .categorie(categorie)
                            .parent(parent)
                            .statutPaiement(Joueur.StatutPaiement.A_JOUR)
                            .build();
                    joueurRepository.save(joueur);
                    result.joueursImported++;

                } catch (Exception e) {
                    result.messages.add("Ligne " + lineNumber + ": " + e.getMessage());
                    result.erreurs++;
                }
            }
        }

        log.info("Import terminé: {} joueurs, {} parents, {} erreurs",
                result.joueursImported, result.parentsImported, result.erreurs);
        return result;
    }

    @Transactional
    public ImportResult importPaymentsCsv(InputStream inputStream) throws IOException {
        ImportResult result = new ImportResult();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String header = reader.readLine();
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 5) {
                        result.messages.add("Ligne " + lineNumber + ": colonnes manquantes");
                        result.erreurs++;
                        continue;
                    }

                    String joueurNom = parts[0].trim();
                    String joueurPrenom = parts[1].trim();
                    BigDecimal montant = new BigDecimal(parts[2].trim());
                    LocalDate dateEcheance = LocalDate.parse(parts[3].trim(), fmt);
                    String statutStr = parts[4].trim();

                    List<Joueur> joueurs = joueurRepository
                            .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(joueurNom, joueurPrenom, org.springframework.data.domain.PageRequest.of(0, 1))
                            .getContent();

                    if (joueurs.isEmpty()) {
                        result.messages.add("Ligne " + lineNumber + ": joueur non trouvé " + joueurPrenom + " " + joueurNom);
                        result.erreurs++;
                        continue;
                    }

                    Joueur joueur = joueurs.get(0);
                    StatutPaiement statut = switch (statutStr.toUpperCase()) {
                        case "PAYE", "PAID" -> StatutPaiement.PAYE;
                        case "EN_RETARD", "OVERDUE" -> StatutPaiement.EN_RETARD;
                        default -> StatutPaiement.EN_ATTENTE;
                    };

                    Paiement paiement = Paiement.builder()
                            .joueur(joueur)
                            .parent(joueur.getParent())
                            .montant(montant)
                            .devise("TND")
                            .dateEcheance(dateEcheance)
                            .statut(statut)
                            .build();
                    paiementRepository.save(paiement);
                    result.paiementsImported++;

                } catch (Exception e) {
                    result.messages.add("Ligne " + lineNumber + ": " + e.getMessage());
                    result.erreurs++;
                }
            }
        }

        log.info("Import paiements terminé: {} paiements, {} erreurs",
                result.paiementsImported, result.erreurs);
        return result;
    }
}
