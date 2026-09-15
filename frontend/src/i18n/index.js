import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = {
  fr: {
    translation: {
      app: { name: 'Nadi' },
      nav: {
        dashboard: 'Tableau de bord',
        players: 'Joueurs',
        parents: 'Parents',
        payments: 'Paiements',
        training: 'Entraînements',
        coaches: 'Entraîneurs',
        categories: 'Catégories & Créneaux',
        alerts: 'Alertes',
        settings: 'Paramètres',
        import: 'Import',
        logout: 'Déconnexion'
      },
      auth: {
        login: 'Connexion',
        email: 'Email',
        password: 'Mot de passe',
        loginButton: 'Se connecter',
        loginError: 'Email ou mot de passe incorrect',
        logout: 'Déconnexion'
      },
      common: {
        save: 'Enregistrer',
        cancel: 'Annuler',
        delete: 'Supprimer',
        edit: 'Modifier',
        add: 'Ajouter',
        search: 'Rechercher',
        loading: 'Chargement...',
        noData: 'Aucune donnée',
        confirm: 'Confirmer',
        back: 'Retour',
        filter: 'Filtrer',
        export: 'Exporter',
        import: 'Importer'
      },
      players: {
        title: 'Joueurs',
        newPlayer: 'Nouveau joueur',
        firstName: 'Prénom',
        lastName: 'Nom',
        birthDate: 'Date de naissance',
        category: 'Catégorie',
        parent: 'Parent',
        status: 'Statut',
        photo: 'Photo'
      },
      categories: {
        title: 'Catégories',
        name: 'Nom',
        description: 'Description',
        ageRange: 'Tranche d\'âge'
      },
      coaches: {
        title: 'Entraîneurs',
        firstName: 'Prénom',
        lastName: 'Nom',
        specialty: 'Spécialité',
        phone: 'Téléphone',
        email: 'Email'
      },
      payments: {
        title: 'Paiements',
        amount: 'Montant',
        currency: 'Devise',
        dueDate: 'Date d\'échéance',
        paymentDate: 'Date de paiement',
        status: 'Statut',
        method: 'Moyen de paiement',
        markPaid: 'Marquer comme payé'
      },
      dashboard: {
        title: 'Tableau de bord',
        totalPlayers: 'Total joueurs',
        activePlayers: 'Joueurs actifs',
        pendingPayments: 'Paiements en attente',
        complianceAlerts: 'Alertes conformité'
      }
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'fr',
    fallbackLng: 'fr',
    interpolation: { escapeValue: false }
  });

export default i18n;
