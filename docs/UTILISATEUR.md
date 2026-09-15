# Guide Utilisateur — Nadi

## Table des matières
1. [Administration (Direction)](#admin)
2. [Entraîneurs](#coach)
3. [Parents](#parent)
4. [Installation PWA](#pwa)
5. [FAQ](#faq)

---

## 1. Administration (Direction) {#admin}

### Connexion
1. Ouvrez l'application dans votre navigateur
2. Entrez votre email et mot de passe
3. Cliquez sur "Se connecter"

### Créer une académie (Nouveau tenant)
1. Allez sur `/onboarding` (pas de connexion requise)
2. Remplissez le nom de l'académie, ville, téléphone
3. Créez votre compte administrateur (email + mot de passe)
4. Cliquez "Créer l'académie" → puis "Se connecter"
5. 5 catégories par défaut sont créées automatiquement (U9, U13, U17, Élite, Généraux)

### Super Administrateur
- **Connexion** : `superadmin@nadi.tn` / `superadmin123`
- **Dashboard** : Vue globale de toutes les académies (stats croisées)
- **Académies** : Activer/désactiver, voir le plan, gérer les invitations
- **Invitations** : Envoyer des liens d'invitation pour créer de nouveaux tenants

### Tableau de bord
- Vue d'ensemble : joueurs, parents, entraîneurs, catégories
- Revenus du mois et montants en attente
- Alertes conformité (documents expirés/expirant bientôt)
- Graphiques : joueurs par catégorie, revenus mensuels

### Gestion des joueurs
1. **Ajouter** : Cliquez "Nouveau joueur" → remplissez le formulaire → sélectionnez parent et catégorie
2. **Modifier** : Cliquez l'icône crayon sur la ligne du joueur
3. **Supprimer** : Cliquez l'icône poubelle (irréversible)
4. **Rechercher** : Utilisez la barre de recherche par nom/prénom

### Gestion des parents
1. **Ajouter** : "Nouveau parent" → prénom, nom, téléphone, email
2. **Consentement RGPD** : Cochez la case si le parent consent
3. **Lien parent-enfant** : Créé automatiquement lors de l'ajout d'un joueur

### Gestion des catégories
1. **Créer** : "Nouvelle catégorie" → nom (ex: U10-U12), description, tranche d'âge
2. **Créneaux** : Ajoutez des créneaux via la section "Créneaux" de la même page
3. **Calendrier** : Le planning s'affiche automatiquement

### Gestion des entraîneurs
1. **Ajouter** : "Nouvel entraîneur" → prénom, nom, spécialité, téléphone, email
2. **Catégories** : Sélectionnez les catégories encadrées (multi-sélection)
3. **Suppression** : Bloquée si des créneaux sont assignés

### Paiements
1. **Générer un échéancier** : Depuis la fiche joueur ou l'API
2. **Marquer comme payé** : Cliquez "Marquer payé" sur la ligne
3. **Filtrer** : Par statut (en attente, payé, en retard)
4. **Exporter** : Cliquez "Exporter" → sélectionnez les dates → CSV ou Excel

### Documents & Conformité
1. **Ajouter un document** : Sélectionnez le joueur, le type, uploadez le fichier
2. **Types** : Certificat médical, Licence FTF, Autorisation parentale, Consentement image
3. **Alertes** : Les documents expirant dans 30 jours déclenchent une notification

### Reporting
1. **Statistiques** : Page Dashboard → "Exporter stats" pour CSV
2. **Registre RGPD** : Menu "RGPD" → voir les consentements → Exporter PDF
3. **Audit** : Consultez les logs d'audit via `/api/audit`

### Facturation
1. **Plan actuel** : Menu "Facturation" → voir l'utilisation vs limites
2. **Changer de plan** : Cliquez sur le plan souhaité → confirmer
3. **Factures** : Historique des factures et paiements

### Import de données
1. **Joueurs** : Menu "Import" → sélectionnez un fichier CSV (prenom,nom,dateNaissance,categorie,parentEmail)
2. **Paiements** : Format CSV (joueurNom,joueurPrenom,montant,dateEcheance,statut)
3. **Validation** : Les erreurs sont affichées après l'import

### Notifications push
- Les notifications push sont envoyées automatiquement pour : paiements en retard, changements d'horaire, compétitions
- Les administrateurs reçoivent les alertes de documents expirés

---

## 2. Entraîneurs {#coach}

### Accès
- Vos catégories et créneaux assignés
- Liste des joueurs de vos catégories
- Pas d'accès aux paiements ni aux données financières

### Planning
- Consultez le calendrier hebdomadaire dans l'onglet "Entraînements"
- Les créneaux affichent la catégorie, le terrain et l'horaire

### Joueurs
- Consultez la liste des joueurs de vos catégories
- Recherchez par nom

---

## 3. Parents {#parent}

### Connexion
- Utilisez les identifiants fournis par l'académie
- Installez l'application sur votre écran d'accueil (voir PWA)

### Mes enfants
- Planning d'entraînements de votre enfant
- Statut des paiements (à jour, en retard, impayé)
- Documents fournis (certificat médical, etc.)

### Paiements
- Consultez les échéances à venir
- Marquez un paiement comme payé (si payé en espèces)

### Hors ligne
- Le planning reste accessible sans connexion internet
- Les données sont mises en cache automatiquement

---

## 4. Installation PWA {#pwa}

### Sur iPhone (Safari)
1. Ouvrez l'application dans Safari
2. Appuyez sur le bouton Partager (ios-share)
3. Sélectionnez "Ajouter à l'écran d'accueil"
4. Confirmez en appuyant sur "Ajouter"

### Sur Android (Chrome)
1. Ouvrez l'application dans Chrome
2. Appuyez sur le menu (3 points)
3. Sélectionnez "Ajouter à l'écran d'accueil"
4. Confirmez en appuyant sur "Ajouter"

### Sur ordinateur
1. Cliquez sur l'icône de verrouillage dans la barre d'adresse
2. Sélectionnez "Installer l'application"

---

## 5. FAQ {#faq}

**Q : J'ai oublié mon mot de passe**
R : Contactez l'administration pour un réinitialisation.

**Q : Le planning n'est pas à jour**
R : Vérifiez votre connexion internet. Si le problème persiste, contactez l'administration.

**Q : Je ne vois pas tous les joueurs**
R : Les parents ne voient que leurs propres enfants. Les coachs ne voient que les joueurs de leurs catégories.

**Q : Comment modifier mes informations personnelles**
R : Contactez l'administration.

**Q : L'application ne fonctionne pas hors ligne**
R : Seules les données mises en cache (planning, dashboard) sont disponibles hors ligne. Les opérations d'écriture nécessitent une connexion.
