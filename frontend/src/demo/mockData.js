let nextId = 200;
const id = () => nextId++;

// ─── Categories ──────────────────────────────────────
export const DEMO_CATEGORIES = [
  { id: 1, nom: 'U7',   description: 'Éveil sportif', maxJoueurs: 16, actif: true },
  { id: 2, nom: 'U9',   description: 'Formation',     maxJoueurs: 20, actif: true },
  { id: 3, nom: 'U11',  description: 'Initiation',    maxJoueurs: 20, actif: true },
  { id: 4, nom: 'U13',  description: 'Perfectionnement', maxJoueurs: 22, actif: true },
  { id: 5, nom: 'U15',  description: 'Compétition',   maxJoueurs: 22, actif: true },
  { id: 6, nom: 'U17',  description: 'Pré-nationale', maxJoueurs: 22, actif: true },
];

// ─── Coaches ─────────────────────────────────────────
export const DEMO_COACHES = [
  { id: 1, prenom: 'Karim',  nom: 'Amri',         specialite: 'CAF C — Développement', telephone: '+216 98 123 456', email: 'karim.amri@nadi.tn', categorieIds: [2, 3],    categories: ['U9', 'U11'] },
  { id: 2, prenom: 'Sami',   nom: 'Bouzid',        specialite: 'CAF B — Tactique',       telephone: '+216 97 234 567', email: 'sami.bouzid@nadi.tn', categorieIds: [4, 5],   categories: ['U13', 'U15'] },
  { id: 3, prenom: 'Wassim', nom: 'Herzi',         specialite: 'Gardiens — Licencié FTF', telephone: '+216 96 345 678', email: 'wassim.herzi@nadi.tn', categorieIds: [2,3,4,5,6], categories: ['U9','U11','U13','U15','U17'] },
  { id: 4, prenom: 'Nabil',  nom: 'Mansour',       specialite: 'CAF A — Performance',     telephone: '+216 95 456 789', email: 'nabil.mansour@nadi.tn', categorieIds: [5, 6],   categories: ['U15', 'U17'] },
  { id: 5, prenom: 'Rachid', nom: 'Ferchichi',     specialite: 'CAF C — Éveil',           telephone: '+216 94 567 890', email: 'rachid.ferchichi@nadi.tn', categorieIds: [1, 2], categories: ['U7', 'U9'] },
];

// ─── Parents ─────────────────────────────────────────
export const DEMO_PARENTS = [
  { id: 1, prenom: 'Salah',   nom: 'Trabelsi',    telephone: '+216 71 234 567', email: 'salah.trabelsi@gmail.com',    consentementRGPD: true,  joueurs: ['Mohamed Ali', 'Yassine'] },
  { id: 2, prenom: 'Nadia',   nom: 'Ben Salem',    telephone: '+216 73 345 678', email: 'nadia.bensalem@yahoo.fr',     consentementRGPD: true,  joueurs: ['Ahmed', 'Ibrahim'] },
  { id: 3, prenom: 'Riadh',   nom: 'Karoui',       telephone: '+216 74 456 789', email: 'riadh.karoui@outlook.com',    consentementRGPD: true,  joueurs: ['Sofien', 'Hamza'] },
  { id: 4, prenom: 'Amel',    nom: 'Gharbi',       telephone: '+216 75 567 890', email: 'amel.gharbi@gmail.com',       consentementRGPD: true,  joueurs: ['Oussama', 'Anis'] },
  { id: 5, prenom: 'Fathi',   nom: 'Bouazizi',     telephone: '+216 76 678 901', email: 'fathi.bouazizi@gmail.com',    consentementRGPD: false, joueurs: ['Zied', 'Firas'] },
  { id: 6, prenom: 'Leila',   nom: 'Messaoudi',    telephone: '+216 77 789 012', email: 'leila.messaoudi@nadi.tn',     consentementRGPD: true,  joueurs: ['Youssef', 'Marouen'] },
  { id: 7, prenom: 'Hichem',  nom: 'Drissi',       telephone: '+216 78 890 123', email: 'hichem.drissi@gmail.com',     consentementRGPD: true,  joueurs: ['Aziz', 'Rania'] },
  { id: 8, prenom: 'Siham',   nom: 'Lajmi',        telephone: '+216 79 901 234', email: 'siham.lajmi@yahoo.fr',        consentementRGPD: true,  joueurs: ['Mehdi'] },
  { id: 9, prenom: 'Walid',   nom: 'Chedli',       telephone: '+216 91 012 345', email: 'walid.chedli@outlook.com',    consentementRGPD: true,  joueurs: ['Tarek', 'Skander'] },
  { id: 10, prenom: 'Fatma',  nom: 'Zouari',       telephone: '+216 92 123 456', email: 'fatma.zouari@gmail.com',      consentementRGPD: false, joueurs: ['Amine', 'Omar'] },
];

// ─── Players ─────────────────────────────────────────
export const DEMO_PLAYERS = [
  // U7 — Éveil sportif (ages 5-7)
  { id: 1,  prenom: 'Youssef',  nom: 'Messaoudi',  dateNaissance: '2019-03-12', dateEntree: '2025-09-05', categorieId: 1, categorieNom: 'U7',  parentId: 6, parentPrenom: 'Leila',   parentNom: 'Messaoudi', statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 2,  prenom: 'Anis',    nom: 'Bouchama',   dateNaissance: '2019-06-20', dateEntree: '2025-09-10', categorieId: 1, categorieNom: 'U7',  parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 3,  prenom: 'Zied',    nom: 'Ayari',      dateNaissance: '2019-05-29', dateEntree: '2025-09-01', categorieId: 1, categorieNom: 'U7',  parentId: 5, parentPrenom: 'Fathi',   parentNom: 'Bouazizi',   statutPaiement: 'EN_ATTENTE', frequence: 'MENSUEL', moisAVerser: 12, moisPayes: 11, moisImpayes: 1, certificatMedical: false, autorisationParentale: false },

  // U9 — Formation (ages 7-9)
  { id: 4,  prenom: 'Mohamed Ali', nom: 'Trabelsi', dateNaissance: '2017-03-15', dateEntree: '2024-09-02', categorieId: 2, categorieNom: 'U9',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',   statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 5,  prenom: 'Yassine',   nom: 'Trabelsi',  dateNaissance: '2018-07-22', dateEntree: '2024-09-02', categorieId: 2, categorieNom: 'U9',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',   statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 6,  prenom: 'Aziz',     nom: 'Drissi',     dateNaissance: '2018-12-01', dateEntree: '2025-01-10', categorieId: 2, categorieNom: 'U9',  parentId: 7, parentPrenom: 'Hichem',  parentNom: 'Drissi',     statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 7,  prenom: 'Ibrahim',  nom: 'Ben Salem',   dateNaissance: '2017-01-05', dateEntree: '2025-09-01', categorieId: 2, categorieNom: 'U9',  parentId: 2, parentPrenom: 'Nadia',   parentNom: 'Ben Salem',   statutPaiement: 'EN_ATTENTE', frequence: 'MENSUEL', moisAVerser: 12, moisPayes: 11, moisImpayes: 1, certificatMedical: false, autorisationParentale: true  },

  // U11 — Initiation (ages 9-11)
  { id: 8,  prenom: 'Rania',    nom: 'Chedli',     dateNaissance: '2015-04-17', dateEntree: '2023-09-05', categorieId: 3, categorieNom: 'U11', parentId: 9, parentPrenom: 'Walid',   parentNom: 'Chedli',     statutPaiement: 'A_JOUR',   frequence: 'TRIMESTRIEL', moisAVerser: 4,  moisPayes: 4,  moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 9,  prenom: 'Mehdi',    nom: 'Lajmi',      dateNaissance: '2015-08-25', dateEntree: '2024-01-15', categorieId: 3, categorieNom: 'U11', parentId: 8, parentPrenom: 'Siham',   parentNom: 'Lajmi',      statutPaiement: 'EN_RETARD', frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 9,  moisImpayes: 3, certificatMedical: true,  autorisationParentale: false },
  { id: 10, prenom: 'Amine',    nom: 'Zouari',     dateNaissance: '2015-10-30', dateEntree: '2024-09-01', categorieId: 3, categorieNom: 'U11', parentId: 10, parentPrenom: 'Fatma',   parentNom: 'Zouari',     statutPaiement: 'EN_RETARD', frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 10, moisImpayes: 2, certificatMedical: true,  autorisationParentale: false },

  // U13 — Perfectionnement (ages 11-13)
  { id: 11, prenom: 'Ahmed',    nom: 'Karoui',     dateNaissance: '2013-11-08', dateEntree: '2022-09-01', categorieId: 4, categorieNom: 'U13', parentId: 2, parentPrenom: 'Nadia',   parentNom: 'Ben Salem',   statutPaiement: 'A_JOUR',   frequence: 'TRIMESTRIEL', moisAVerser: 4,  moisPayes: 3,  moisImpayes: 1, certificatMedical: true,  autorisationParentale: true  },
  { id: 12, prenom: 'Sofien',   nom: 'Gharbi',     dateNaissance: '2013-05-19', dateEntree: '2022-09-01', categorieId: 4, categorieNom: 'U13', parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     statutPaiement: 'EN_RETARD', frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 8,  moisImpayes: 4, certificatMedical: true,  autorisationParentale: true  },
  { id: 13, prenom: 'Firas',    nom: 'Jaziri',     dateNaissance: '2013-09-12', dateEntree: '2024-01-10', categorieId: 4, categorieNom: 'U13', parentId: 5, parentPrenom: 'Fathi',   parentNom: 'Bouazizi',   statutPaiement: 'A_JOUR',   frequence: 'TRIMESTRIEL', moisAVerser: 4,  moisPayes: 4,  moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 14, prenom: 'Marouen',  nom: 'Haddad',     dateNaissance: '2012-03-08', dateEntree: '2023-09-01', categorieId: 4, categorieNom: 'U13', parentId: 6, parentPrenom: 'Leila',   parentNom: 'Messaoudi',  statutPaiement: 'EN_RETARD', frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 9,  moisImpayes: 3, certificatMedical: true,  autorisationParentale: false },

  // U15 — Compétition (ages 13-15)
  { id: 15, prenom: 'Hamza',    nom: 'Bouazizi',   dateNaissance: '2011-09-03', dateEntree: '2021-09-01', categorieId: 5, categorieNom: 'U15', parentId: 3, parentPrenom: 'Riadh',   parentNom: 'Karoui',     statutPaiement: 'A_JOUR',   frequence: 'SEMESTRIEL', moisAVerser: 2,  moisPayes: 2,  moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 16, prenom: 'Oussama',  nom: 'Gharbi',     dateNaissance: '2011-01-28', dateEntree: '2021-09-01', categorieId: 5, categorieNom: 'U15', parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     statutPaiement: 'A_JOUR',   frequence: 'MENSUEL',   moisAVerser: 12, moisPayes: 12, moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 17, prenom: 'Skander',  nom: 'Chedli',     dateNaissance: '2011-07-16', dateEntree: '2022-09-01', categorieId: 5, categorieNom: 'U15', parentId: 9, parentPrenom: 'Walid',   parentNom: 'Chedli',     statutPaiement: 'A_JOUR',   frequence: 'TRIMESTRIEL', moisAVerser: 4,  moisPayes: 4,  moisImpayes: 0, certificatMedical: false, autorisationParentale: true  },

  // U17 — Pré-nationale (ages 15-17)
  { id: 18, prenom: 'Tarek',    nom: 'Drissi',     dateNaissance: '2009-02-10', dateEntree: '2020-09-01', categorieId: 6, categorieNom: 'U17', parentId: 7, parentPrenom: 'Hichem',  parentNom: 'Drissi',     statutPaiement: 'A_JOUR',   frequence: 'SEMESTRIEL', moisAVerser: 2,  moisPayes: 2,  moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 19, prenom: 'Omar',     nom: 'Bennour',    dateNaissance: '2009-04-22', dateEntree: '2021-09-01', categorieId: 6, categorieNom: 'U17', parentId: 10, parentPrenom: 'Fatma',   parentNom: 'Zouari',     statutPaiement: 'A_JOUR',   frequence: 'ANNUEL',   moisAVerser: 1,  moisPayes: 1,  moisImpayes: 0, certificatMedical: true,  autorisationParentale: true  },
  { id: 20, prenom: 'Walid',    nom: 'Sghaier',    dateNaissance: '2009-11-11', dateEntree: '2020-09-01', categorieId: 6, categorieNom: 'U17', parentId: 3, parentPrenom: 'Riadh',   parentNom: 'Karoui',     statutPaiement: 'EN_ATTENTE', frequence: 'SEMESTRIEL', moisAVerser: 2,  moisPayes: 1,  moisImpayes: 1, certificatMedical: true,  autorisationParentale: true  },
];

// ─── Payments ────────────────────────────────────────
export const DEMO_PAYMENTS = [
  // Mohamed Ali Trabelsi — MENSUEL, à jour
  { id: 1,  joueurId: 4,  joueurPrenom: 'Mohamed Ali', joueurNom: 'Trabelsi',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',  montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-08-28', dateEncaissement: '2026-08-28', statut: 'PAYE',     moyenPaiement: 'ESPECES',       formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-001', numeroRecu: 'REC-001', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-28T10:00:00' },
  { id: 2,  joueurId: 4,  joueurPrenom: 'Mohamed Ali', joueurNom: 'Trabelsi',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',  montant: 60,  devise: 'TND', dateEcheance: '2026-08-01', datePaiement: '2026-07-29', dateEncaissement: '2026-07-29', statut: 'PAYE',     moyenPaiement: 'ESPECES',       formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-010', numeroRecu: 'REC-010', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-07-29T10:00:00' },
  { id: 3,  joueurId: 4,  joueurPrenom: 'Mohamed Ali', joueurNom: 'Trabelsi',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',  montant: 60,  devise: 'TND', dateEcheance: '2026-07-01', datePaiement: '2026-06-28', dateEncaissement: '2026-06-28', statut: 'PAYE',     moyenPaiement: 'ESPECES',       formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-020', numeroRecu: 'REC-020', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-06-28T10:00:00' },

  // Yassine Trabelsi — MENSUEL, à jour
  { id: 4,  joueurId: 5,  joueurPrenom: 'Yassine',    joueurNom: 'Trabelsi',  parentId: 1, parentPrenom: 'Salah',   parentNom: 'Trabelsi',  montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-09-02', dateEncaissement: '2026-09-02', statut: 'PAYE',     moyenPaiement: 'VIREMENT',      formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-002', numeroRecu: 'REC-002', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-02T14:30:00' },

  // Ahmed Karoui — TRIMESTRIEL, retard 1 mois
  { id: 5,  joueurId: 11, joueurPrenom: 'Ahmed',      joueurNom: 'Karoui',    parentId: 2, parentPrenom: 'Nadia',   parentNom: 'Ben Salem',  montant: 180, devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_ATTENTE', moyenPaiement: null,             formule: 'TRIMESTRIEL', frequence: 'TRIMESTRIEL', reference: null,             numeroRecu: null,     commentaire: 'Trimestre Q3',   notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T09:00:00' },
  { id: 6,  joueurId: 11, joueurPrenom: 'Ahmed',      joueurNom: 'Karoui',    parentId: 2, parentPrenom: 'Nadia',   parentNom: 'Ben Salem',  montant: 180, devise: 'TND', dateEcheance: '2026-06-01', datePaiement: '2026-06-03', dateEncaissement: '2026-06-03', statut: 'PAYE',     moyenPaiement: 'VIREMENT',      formule: 'TRIMESTRIEL', frequence: 'TRIMESTRIEL', reference: 'PAI-2026-030', numeroRecu: 'REC-030', commentaire: 'Trimestre Q2',   notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-06-03T11:00:00' },

  // Sofien Gharbi — MENSUEL, en retard 4 mois
  { id: 7,  joueurId: 12, joueurPrenom: 'Sofien',     joueurNom: 'Gharbi',    parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: 'Relance le 05/09', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },
  { id: 8,  joueurId: 12, joueurPrenom: 'Sofien',     joueurNom: 'Gharbi',    parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     montant: 60,  devise: 'TND', dateEcheance: '2026-08-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '2e relance envoyée', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-01T08:00:00' },
  { id: 9,  joueurId: 12, joueurPrenom: 'Sofien',     joueurNom: 'Gharbi',    parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     montant: 60,  devise: 'TND', dateEcheance: '2026-07-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-07-01T08:00:00' },
  { id: 10, joueurId: 12, joueurPrenom: 'Sofien',     joueurNom: 'Gharbi',    parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     montant: 60,  devise: 'TND', dateEcheance: '2026-06-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-06-01T08:00:00' },

  // Hamza Bouazizi — SEMESTRIEL, à jour
  { id: 11, joueurId: 15, joueurPrenom: 'Hamza',      joueurNom: 'Bouazizi',  parentId: 3, parentPrenom: 'Riadh',   parentNom: 'Karoui',     montant: 360, devise: 'TND', dateEcheance: '2026-09-15', datePaiement: '2026-09-14', dateEncaissement: '2026-09-14', statut: 'PAYE',     moyenPaiement: 'CARTE_BANCAIRE', formule: 'SEMESTRIEL', frequence: 'SEMESTRIEL', reference: 'PAI-2026-011', numeroRecu: 'REC-011', commentaire: 'Semestre 2',  notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-14T13:45:00' },

  // Oussama Gharbi — MENSUEL, à jour
  { id: 12, joueurId: 16, joueurPrenom: 'Oussama',    joueurNom: 'Gharbi',    parentId: 4, parentPrenom: 'Amel',    parentNom: 'Gharbi',     montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-09-01', dateEncaissement: '2026-09-01', statut: 'PAYE',     moyenPaiement: 'CARTE_BANCAIRE', formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-012', numeroRecu: 'REC-012', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T16:00:00' },

  // Youssef Drissi — ANNUEL, à jour
  { id: 13, joueurId: 18, joueurPrenom: 'Tarek',      joueurNom: 'Drissi',    parentId: 7, parentPrenom: 'Hichem',  parentNom: 'Drissi',     montant: 720, devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-08-25', dateEncaissement: '2026-08-25', statut: 'PAYE',     moyenPaiement: 'VIREMENT',      formule: 'ANNUEL',    frequence: 'SEMESTRIEL', reference: 'PAI-2026-013', numeroRecu: 'REC-013', commentaire: 'Année complète', notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-25T11:00:00' },

  // Aziz Drissi — MENSUEL, à jour
  { id: 14, joueurId: 6,  joueurPrenom: 'Aziz',       joueurNom: 'Drissi',    parentId: 7, parentPrenom: 'Hichem',  parentNom: 'Drissi',     montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-09-03', dateEncaissement: '2026-09-03', statut: 'PAYE',     moyenPaiement: 'ESPECES',       formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: 'PAI-2026-014', numeroRecu: 'REC-014', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-03T09:15:00' },

  // Rania Chedli — TRIMESTRIEL, à jour
  { id: 15, joueurId: 8,  joueurPrenom: 'Rania',      joueurNom: 'Chedli',    parentId: 9, parentPrenom: 'Walid',   parentNom: 'Chedli',     montant: 180, devise: 'TND', dateEcheance: '2026-08-20', datePaiement: '2026-08-19', dateEncaissement: '2026-08-19', statut: 'PAYE',     moyenPaiement: 'VIREMENT',      formule: 'TRIMESTRIEL', frequence: 'TRIMESTRIEL', reference: 'PAI-2026-015', numeroRecu: 'REC-015', commentaire: 'Trimestre Q3', notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-19T15:00:00' },

  // Mehdi Lajmi — MENSUEL, retard 3 mois
  { id: 16, joueurId: 9,  joueurPrenom: 'Mehdi',      joueurNom: 'Lajmi',     parentId: 8, parentPrenom: 'Siham',   parentNom: 'Lajmi',      montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '3 mois impayés', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },
  { id: 17, joueurId: 9,  joueurPrenom: 'Mehdi',      joueurNom: 'Lajmi',     parentId: 8, parentPrenom: 'Siham',   parentNom: 'Lajmi',      montant: 60,  devise: 'TND', dateEcheance: '2026-08-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-01T08:00:00' },
  { id: 18, joueurId: 9,  joueurPrenom: 'Mehdi',      joueurNom: 'Lajmi',     parentId: 8, parentPrenom: 'Siham',   parentNom: 'Lajmi',      montant: 60,  devise: 'TND', dateEcheance: '2026-07-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-07-01T08:00:00' },

  // Amine Zouari — MENSUEL, retard 2 mois
  { id: 19, joueurId: 10, joueurPrenom: 'Amine',      joueurNom: 'Zouari',    parentId: 10, parentPrenom: 'Fatma',   parentNom: 'Zouari',     montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '2 mois impayés', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },
  { id: 20, joueurId: 10, joueurPrenom: 'Amine',      joueurNom: 'Zouari',    parentId: 10, parentPrenom: 'Fatma',   parentNom: 'Zouari',     montant: 60,  devise: 'TND', dateEcheance: '2026-08-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-01T08:00:00' },

  // Firas Jaziri — TRIMESTRIEL, à jour
  { id: 21, joueurId: 13, joueurPrenom: 'Firas',      joueurNom: 'Jaziri',    parentId: 5, parentPrenom: 'Fathi',   parentNom: 'Bouazizi',   montant: 180, devise: 'TND', dateEcheance: '2026-09-15', datePaiement: '2026-09-14', dateEncaissement: '2026-09-14', statut: 'PAYE',     moyenPaiement: 'CARTE_BANCAIRE', formule: 'TRIMESTRIEL', frequence: 'TRIMESTRIEL', reference: 'PAI-2026-021', numeroRecu: 'REC-021', commentaire: 'Trimestre Q3', notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-14T13:45:00' },

  // Ibrahim Ben Salem — MENSUEL, retard 1 mois
  { id: 22, joueurId: 7,  joueurPrenom: 'Ibrahim',    joueurNom: 'Ben Salem',  parentId: 2, parentPrenom: 'Nadia',   parentNom: 'Ben Salem',  montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_ATTENTE', moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: 'Nouvelle inscription', notes: '', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },

  // Skander Chedli — TRIMESTRIEL, à jour
  { id: 23, joueurId: 17, joueurPrenom: 'Skander',    joueurNom: 'Chedli',    parentId: 9, parentPrenom: 'Walid',   parentNom: 'Chedli',     montant: 180, devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-08-30', dateEncaissement: '2026-08-30', statut: 'PAYE',     moyenPaiement: 'ESPECES',       formule: 'TRIMESTRIEL', frequence: 'TRIMESTRIEL', reference: 'PAI-2026-023', numeroRecu: 'REC-023', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-30T10:00:00' },

  // Walid Sghaier — SEMESTRIEL, retard 1 mois
  { id: 24, joueurId: 20, joueurPrenom: 'Walid',      joueurNom: 'Sghaier',   parentId: 3, parentPrenom: 'Riadh',   parentNom: 'Karoui',     montant: 360, devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_ATTENTE', moyenPaiement: null,             formule: 'SEMESTRIEL', frequence: 'SEMESTRIEL', reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },

  // Marouen Haddad — MENSUEL, retard 3 mois
  { id: 25, joueurId: 14, joueurPrenom: 'Marouen',    joueurNom: 'Haddad',    parentId: 6, parentPrenom: 'Leila',   parentNom: 'Messaoudi',  montant: 60,  devise: 'TND', dateEcheance: '2026-09-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '3 mois impayés', enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-09-01T08:00:00' },
  { id: 26, joueurId: 14, joueurPrenom: 'Marouen',    joueurNom: 'Haddad',    parentId: 6, parentPrenom: 'Leila',   parentNom: 'Messaoudi',  montant: 60,  devise: 'TND', dateEcheance: '2026-08-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-01T08:00:00' },
  { id: 27, joueurId: 14, joueurPrenom: 'Marouen',    joueurNom: 'Haddad',    parentId: 6, parentPrenom: 'Leila',   parentNom: 'Messaoudi',  montant: 60,  devise: 'TND', dateEcheance: '2026-07-01', datePaiement: null,          dateEncaissement: null,          statut: 'EN_RETARD',  moyenPaiement: null,             formule: 'MENSUEL',    frequence: 'MENSUEL',    reference: null,             numeroRecu: null,     commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-07-01T08:00:00' },

  // Omar Bennour — ANNUEL, à jour
  { id: 28, joueurId: 19, joueurPrenom: 'Omar',       joueurNom: 'Bennour',   parentId: 10, parentPrenom: 'Fatma',   parentNom: 'Zouari',     montant: 720, devise: 'TND', dateEcheance: '2026-09-01', datePaiement: '2026-08-20', dateEncaissement: '2026-08-20', statut: 'PAYE',     moyenPaiement: 'VIREMENT',      formule: 'ANNUEL',    frequence: 'ANNUEL',    reference: 'PAI-2026-028', numeroRecu: 'REC-028', commentaire: '',          notes: '',          enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn', createdAt: '2026-08-20T10:00:00' },
];

// ─── Training Slots ──────────────────────────────────
export const DEMO_SLOTS = [
  // Lundi
  { id: 1,  jourSemaine: 'LUNDI',    heureDebut: '16:00', heureFin: '17:00', categorieId: 1, categorieNom: 'U7',  entraineurId: 5, entraineurPrenom: 'Rachid', entraineurNom: 'Ferchichi', terrain: 'Terrain B', capacite: 16 },
  { id: 2,  jourSemaine: 'LUNDI',    heureDebut: '16:00', heureFin: '17:30', categorieId: 2, categorieNom: 'U9',  entraineurId: 1, entraineurPrenom: 'Karim',  entraineurNom: 'Amri',      terrain: 'Terrain A', capacite: 20 },
  { id: 3,  jourSemaine: 'LUNDI',    heureDebut: '17:30', heureFin: '19:00', categorieId: 4, categorieNom: 'U13', entraineurId: 2, entraineurPrenom: 'Sami',   entraineurNom: 'Bouzid',    terrain: 'Terrain A', capacite: 22 },
  { id: 4,  jourSemaine: 'LUNDI',    heureDebut: '19:00', heureFin: '20:30', categorieId: 6, categorieNom: 'U17', entraineurId: 4, entraineurPrenom: 'Nabil',  entraineurNom: 'Mansour',   terrain: 'Terrain A', capacite: 22 },

  // Mardi
  { id: 5,  jourSemaine: 'MARDI',    heureDebut: '16:00', heureFin: '17:30', categorieId: 3, categorieNom: 'U11', entraineurId: 1, entraineurPrenom: 'Karim',  entraineurNom: 'Amri',      terrain: 'Terrain B', capacite: 20 },
  { id: 6,  jourSemaine: 'MARDI',    heureDebut: '17:30', heureFin: '19:00', categorieId: 5, categorieNom: 'U15', entraineurId: 4, entraineurPrenom: 'Nabil',  entraineurNom: 'Mansour',   terrain: 'Terrain A', capacite: 22 },
  { id: 7,  jourSemaine: 'MARDI',    heureDebut: '19:00', heureFin: '20:30', categorieId: 4, categorieNom: 'U13', entraineurId: 3, entraineurPrenom: 'Wassim', entraineurNom: 'Herzi',     terrain: 'Salle omnisports', capacite: 22 },

  // Mercredi
  { id: 8,  jourSemaine: 'MERCREDI', heureDebut: '16:00', heureFin: '17:00', categorieId: 1, categorieNom: 'U7',  entraineurId: 5, entraineurPrenom: 'Rachid', entraineurNom: 'Ferchichi', terrain: 'Terrain B', capacite: 16 },
  { id: 9,  jourSemaine: 'MERCREDI', heureDebut: '16:00', heureFin: '17:30', categorieId: 2, categorieNom: 'U9',  entraineurId: 1, entraineurPrenom: 'Karim',  entraineurNom: 'Amri',      terrain: 'Terrain A', capacite: 20 },
  { id: 10, jourSemaine: 'MERCREDI', heureDebut: '17:30', heureFin: '19:00', categorieId: 5, categorieNom: 'U15', entraineurId: 2, entraineurPrenom: 'Sami',   entraineurNom: 'Bouzid',    terrain: 'Terrain A', capacite: 22 },

  // Jeudi
  { id: 11, jourSemaine: 'JEUDI',    heureDebut: '16:00', heureFin: '17:30', categorieId: 3, categorieNom: 'U11', entraineurId: 1, entraineurPrenom: 'Karim',  entraineurNom: 'Amri',      terrain: 'Terrain B', capacite: 20 },
  { id: 12, jourSemaine: 'JEUDI',    heureDebut: '17:30', heureFin: '19:00', categorieId: 6, categorieNom: 'U17', entraineurId: 3, entraineurPrenom: 'Wassim', entraineurNom: 'Herzi',     terrain: 'Terrain A', capacite: 22 },
  { id: 13, jourSemaine: 'JEUDI',    heureDebut: '19:00', heureFin: '20:30', categorieId: 5, categorieNom: 'U15', entraineurId: 4, entraineurPrenom: 'Nabil',  entraineurNom: 'Mansour',   terrain: 'Terrain A', capacite: 22 },

  // Vendredi
  { id: 14, jourSemaine: 'VENDREDI', heureDebut: '16:00', heureFin: '17:00', categorieId: 1, categorieNom: 'U7',  entraineurId: 5, entraineurPrenom: 'Rachid', entraineurNom: 'Ferchichi', terrain: 'Terrain B', capacite: 16 },
  { id: 15, jourSemaine: 'VENDREDI', heureDebut: '17:00', heureFin: '18:30', categorieId: 2, categorieNom: 'U9',  entraineurId: 1, entraineurPrenom: 'Karim',  entraineurNom: 'Amri',      terrain: 'Terrain A', capacite: 20 },
  { id: 16, jourSemaine: 'VENDREDI', heureDebut: '18:30', heureFin: '20:00', categorieId: 4, categorieNom: 'U13', entraineurId: 2, entraineurPrenom: 'Sami',   entraineurNom: 'Bouzid',    terrain: 'Terrain A', capacite: 22 },
];

// ─── Attendance Stats (per player) ───────────────────
export const DEMO_ATTENDANCE_STATS = [
  // U7
  { joueurId: 1,  joueurPrenom: 'Youssef',  joueurNom: 'Messaoudi', mois: { attendanceRate: 92, presentCount: 11, totalSessions: 12 }, trimestre: { attendanceRate: 88, presentCount: 22, totalSessions: 25 }, semestre: { attendanceRate: 85, presentCount: 42, totalSessions: 50 }, annee: { attendanceRate: 85, presentCount: 42, totalSessions: 50 }, allTime: { attendanceRate: 85, presentCount: 42, totalSessions: 50 } },
  { joueurId: 2,  joueurPrenom: 'Anis',     joueurNom: 'Bouchama',  mois: { attendanceRate: 100, presentCount: 12, totalSessions: 12 }, trimestre: { attendanceRate: 96, presentCount: 24, totalSessions: 25 }, semestre: { attendanceRate: 90, presentCount: 45, totalSessions: 50 }, annee: { attendanceRate: 90, presentCount: 45, totalSessions: 50 }, allTime: { attendanceRate: 90, presentCount: 45, totalSessions: 50 } },
  { joueurId: 3,  joueurPrenom: 'Zied',     joueurNom: 'Ayari',     mois: { attendanceRate: 75, presentCount: 9, totalSessions: 12 }, trimestre: { attendanceRate: 72, presentCount: 18, totalSessions: 25 }, semestre: { attendanceRate: 70, presentCount: 35, totalSessions: 50 }, annee: { attendanceRate: 70, presentCount: 35, totalSessions: 50 }, allTime: { attendanceRate: 70, presentCount: 35, totalSessions: 50 } },
  // U9
  { joueurId: 4,  joueurPrenom: 'Mohamed Ali', joueurNom: 'Trabelsi', mois: { attendanceRate: 100, presentCount: 12, totalSessions: 12 }, trimestre: { attendanceRate: 96, presentCount: 24, totalSessions: 25 }, semestre: { attendanceRate: 94, presentCount: 47, totalSessions: 50 }, annee: { attendanceRate: 94, presentCount: 47, totalSessions: 50 }, allTime: { attendanceRate: 94, presentCount: 94, totalSessions: 100 } },
  { joueurId: 5,  joueurPrenom: 'Yassine',   joueurNom: 'Trabelsi',  mois: { attendanceRate: 83, presentCount: 10, totalSessions: 12 }, trimestre: { attendanceRate: 80, presentCount: 20, totalSessions: 25 }, semestre: { attendanceRate: 78, presentCount: 39, totalSessions: 50 }, annee: { attendanceRate: 78, presentCount: 39, totalSessions: 50 }, allTime: { attendanceRate: 78, presentCount: 78, totalSessions: 100 } },
  { joueurId: 6,  joueurPrenom: 'Aziz',     joueurNom: 'Drissi',    mois: { attendanceRate: 92, presentCount: 11, totalSessions: 12 }, trimestre: { attendanceRate: 88, presentCount: 22, totalSessions: 25 }, semestre: { attendanceRate: 86, presentCount: 43, totalSessions: 50 }, annee: { attendanceRate: 86, presentCount: 43, totalSessions: 50 }, allTime: { attendanceRate: 86, presentCount: 43, totalSessions: 50 } },
  { joueurId: 7,  joueurPrenom: 'Ibrahim',  joueurNom: 'Ben Salem',  mois: { attendanceRate: 58, presentCount: 7, totalSessions: 12 }, trimestre: { attendanceRate: 58, presentCount: 7, totalSessions: 12 }, semestre: { attendanceRate: 58, presentCount: 7, totalSessions: 12 }, annee: { attendanceRate: 58, presentCount: 7, totalSessions: 12 }, allTime: { attendanceRate: 58, presentCount: 7, totalSessions: 12 } },
  // U11
  { joueurId: 8,  joueurPrenom: 'Rania',    joueurNom: 'Chedli',    mois: { attendanceRate: 100, presentCount: 8, totalSessions: 8 }, trimestre: { attendanceRate: 95, presentCount: 19, totalSessions: 20 }, semestre: { attendanceRate: 92, presentCount: 37, totalSessions: 40 }, annee: { attendanceRate: 92, presentCount: 37, totalSessions: 40 }, allTime: { attendanceRate: 92, presentCount: 110, totalSessions: 120 } },
  { joueurId: 9,  joueurPrenom: 'Mehdi',    joueurNom: 'Lajmi',     mois: { attendanceRate: 63, presentCount: 5, totalSessions: 8 }, trimestre: { attendanceRate: 60, presentCount: 12, totalSessions: 20 }, semestre: { attendanceRate: 55, presentCount: 22, totalSessions: 40 }, annee: { attendanceRate: 55, presentCount: 22, totalSessions: 40 }, allTime: { attendanceRate: 55, presentCount: 66, totalSessions: 120 } },
  { joueurId: 10, joueurPrenom: 'Amine',    joueurNom: 'Zouari',    mois: { attendanceRate: 75, presentCount: 6, totalSessions: 8 }, trimestre: { attendanceRate: 70, presentCount: 14, totalSessions: 20 }, semestre: { attendanceRate: 68, presentCount: 27, totalSessions: 40 }, annee: { attendanceRate: 68, presentCount: 27, totalSessions: 40 }, allTime: { attendanceRate: 68, presentCount: 82, totalSessions: 120 } },
  // U13
  { joueurId: 11, joueurPrenom: 'Ahmed',    joueurNom: 'Karoui',    mois: { attendanceRate: 88, presentCount: 7, totalSessions: 8 }, trimestre: { attendanceRate: 85, presentCount: 17, totalSessions: 20 }, semestre: { attendanceRate: 82, presentCount: 33, totalSessions: 40 }, annee: { attendanceRate: 82, presentCount: 33, totalSessions: 40 }, allTime: { attendanceRate: 82, presentCount: 99, totalSessions: 120 } },
  { joueurId: 12, joueurPrenom: 'Sofien',   joueurNom: 'Gharbi',    mois: { attendanceRate: 50, presentCount: 4, totalSessions: 8 }, trimestre: { attendanceRate: 45, presentCount: 9, totalSessions: 20 }, semestre: { attendanceRate: 42, presentCount: 17, totalSessions: 40 }, annee: { attendanceRate: 42, presentCount: 17, totalSessions: 40 }, allTime: { attendanceRate: 42, presentCount: 51, totalSessions: 120 } },
  { joueurId: 13, joueurPrenom: 'Firas',    joueurNom: 'Jaziri',    mois: { attendanceRate: 100, presentCount: 8, totalSessions: 8 }, trimestre: { attendanceRate: 95, presentCount: 19, totalSessions: 20 }, semestre: { attendanceRate: 90, presentCount: 36, totalSessions: 40 }, annee: { attendanceRate: 90, presentCount: 36, totalSessions: 40 }, allTime: { attendanceRate: 90, presentCount: 90, totalSessions: 100 } },
  { joueurId: 14, joueurPrenom: 'Marouen',  joueurNom: 'Haddad',    mois: { attendanceRate: 63, presentCount: 5, totalSessions: 8 }, trimestre: { attendanceRate: 55, presentCount: 11, totalSessions: 20 }, semestre: { attendanceRate: 50, presentCount: 20, totalSessions: 40 }, annee: { attendanceRate: 50, presentCount: 20, totalSessions: 40 }, allTime: { attendanceRate: 50, presentCount: 60, totalSessions: 120 } },
  // U15
  { joueurId: 15, joueurPrenom: 'Hamza',    joueurNom: 'Bouazizi',  mois: { attendanceRate: 94, presentCount: 15, totalSessions: 16 }, trimestre: { attendanceRate: 90, presentCount: 27, totalSessions: 30 }, semestre: { attendanceRate: 88, presentCount: 53, totalSessions: 60 }, annee: { attendanceRate: 88, presentCount: 53, totalSessions: 60 }, allTime: { attendanceRate: 88, presentCount: 176, totalSessions: 200 } },
  { joueurId: 16, joueurPrenom: 'Oussama',  joueurNom: 'Gharbi',    mois: { attendanceRate: 81, presentCount: 13, totalSessions: 16 }, trimestre: { attendanceRate: 80, presentCount: 24, totalSessions: 30 }, semestre: { attendanceRate: 78, presentCount: 47, totalSessions: 60 }, annee: { attendanceRate: 78, presentCount: 47, totalSessions: 60 }, allTime: { attendanceRate: 78, presentCount: 156, totalSessions: 200 } },
  { joueurId: 17, joueurPrenom: 'Skander',  joueurNom: 'Chedli',    mois: { attendanceRate: 88, presentCount: 14, totalSessions: 16 }, trimestre: { attendanceRate: 85, presentCount: 26, totalSessions: 30 }, semestre: { attendanceRate: 82, presentCount: 49, totalSessions: 60 }, annee: { attendanceRate: 82, presentCount: 49, totalSessions: 60 }, allTime: { attendanceRate: 82, presentCount: 164, totalSessions: 200 } },
  // U17
  { joueurId: 18, joueurPrenom: 'Tarek',    joueurNom: 'Drissi',    mois: { attendanceRate: 86, presentCount: 12, totalSessions: 14 }, trimestre: { attendanceRate: 82, presentCount: 25, totalSessions: 30 }, semestre: { attendanceRate: 80, presentCount: 48, totalSessions: 60 }, annee: { attendanceRate: 80, presentCount: 48, totalSessions: 60 }, allTime: { attendanceRate: 80, presentCount: 240, totalSessions: 300 } },
  { joueurId: 19, joueurPrenom: 'Omar',     joueurNom: 'Bennour',   mois: { attendanceRate: 79, presentCount: 11, totalSessions: 14 }, trimestre: { attendanceRate: 76, presentCount: 23, totalSessions: 30 }, semestre: { attendanceRate: 74, presentCount: 44, totalSessions: 60 }, annee: { attendanceRate: 74, presentCount: 44, totalSessions: 60 }, allTime: { attendanceRate: 74, presentCount: 222, totalSessions: 300 } },
  { joueurId: 20, joueurPrenom: 'Walid',    joueurNom: 'Sghaier',   mois: { attendanceRate: 93, presentCount: 13, totalSessions: 14 }, trimestre: { attendanceRate: 90, presentCount: 27, totalSessions: 30 }, semestre: { attendanceRate: 88, presentCount: 53, totalSessions: 60 }, annee: { attendanceRate: 88, presentCount: 53, totalSessions: 60 }, allTime: { attendanceRate: 88, presentCount: 264, totalSessions: 300 } },
];

// ─── Events ──────────────────────────────────────────
export const DEMO_EVENTS = [
  { id: 1,  titre: 'Tournoi Inter-Académies Tunis', type: 'TOURNOI', dateDebut: '2026-10-04', dateFin: '2026-10-05', heureDebut: '08:00', heureFin: '17:00', lieu: 'Stade Olympique El Menzah', terrain: 'Terrain 3', description: 'Tournoi annuel avec les académies de Sfax, Sousse et Bizerte. Catégories U13 et U15.', joueurIds: [11, 12, 13, 14, 15, 16, 17], categorieIds: [4, 5] },
  { id: 2,  titre: 'Match Amical — Nadi vs Espérance U17', type: 'MATCH', dateDebut: '2026-09-27', dateFin: '2026-09-27', heureDebut: '15:00', heureFin: '17:00', lieu: 'Terrain A — Nadi Academy', terrain: 'Terrain A', description: 'Match amical préparatoire pour la saison. Ouvert aux parents et supporters.', joueurIds: [18, 19, 20], categorieIds: [6] },
  { id: 3,  titre: 'Stage Vacation — Technique et Tactique', type: 'STAGE', dateDebut: '2026-10-20', dateFin: '2026-10-24', heureDebut: '09:00', heureFin: '12:00', lieu: 'Nadi Academy', terrain: 'Tous terrains', description: 'Stage de vacances scolaires pour les catégories U9 à U13. Focus techniques individuelles.', joueurIds: [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14], categorieIds: [2, 3, 4] },
  { id: 4,  titre: 'Entraînement Spécial Gardiens', type: 'ENTRAINEMENT_SPECIAL', dateDebut: '2026-09-20', dateFin: '2026-09-20', heureDebut: '10:00', heureFin: '12:00', lieu: 'Terrain B', terrain: 'Terrain B', description: 'Séance spéciale avec coach Wassim Herzi pour tous les gardiens de l\'académie.', joueurIds: [], categorieIds: [2, 3, 4, 5, 6] },
  { id: 5,  titre: 'Cérémonie de Remise des Licences', type: 'AUTRE', dateDebut: '2026-11-15', dateFin: '2026-11-15', heureDebut: '18:00', heureFin: '20:00', lieu: 'Salle des fêtes — Nadi Academy', terrain: '', description: 'Remise officielle des licences FTF 2026/2027. Tous les joueurs et parents sont conviés.', joueurIds: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20], categorieIds: [1,2,3,4,5,6] },
];

// ─── Convocations (for events) ───────────────────────
export const DEMO_CONVOCATIONS = [
  // Tournoi Inter-Académies
  { id: 1,  evenementId: 1, joueurId: 11, joueurPrenom: 'Ahmed',   joueurNom: 'Karoui',   statut: 'CONFIRME', dateReponse: '2026-09-20T14:00:00', commentaire: 'Present avec plaisir' },
  { id: 2,  evenementId: 1, joueurId: 12, joueurPrenom: 'Sofien',  joueurNom: 'Gharbi',   statut: 'INVITE',   dateReponse: null, commentaire: '' },
  { id: 3,  evenementId: 1, joueurId: 13, joueurPrenom: 'Firas',   joueurNom: 'Jaziri',   statut: 'CONFIRME', dateReponse: '2026-09-21T09:30:00', commentaire: 'Je serai la' },
  { id: 4,  evenementId: 1, joueurId: 15, joueurPrenom: 'Hamza',   joueurNom: 'Bouazizi', statut: 'CONFIRME', dateReponse: '2026-09-19T16:00:00', commentaire: '' },
  { id: 5,  evenementId: 1, joueurId: 16, joueurPrenom: 'Oussama', joueurNom: 'Gharbi',   statut: 'INVITE',   dateReponse: null, commentaire: '' },
  { id: 6,  evenementId: 1, joueurId: 17, joueurPrenom: 'Skander', joueurNom: 'Chedli',   statut: 'CONFIRME', dateReponse: '2026-09-22T11:00:00', commentaire: '' },
  // Match Amical
  { id: 7,  evenementId: 2, joueurId: 18, joueurPrenom: 'Tarek',   joueurNom: 'Drissi',   statut: 'CONFIRME', dateReponse: '2026-09-23T10:00:00', commentaire: 'Je serai present' },
  { id: 8,  evenementId: 2, joueurId: 19, joueurPrenom: 'Omar',    joueurNom: 'Bennour',  statut: 'REFUSE',   dateReponse: '2026-09-24T08:00:00', commentaire: 'Examens cette semaine' },
  { id: 9,  evenementId: 2, joueurId: 20, joueurPrenom: 'Walid',   joueurNom: 'Sghaier',  statut: 'INVITE',   dateReponse: null, commentaire: '' },
];

// ─── Notifications ───────────────────────────────────
export const DEMO_NOTIFICATIONS = [
  { id: 1,  titre: 'Paiements en retard',        message: '6 joueurs ont des paiements en retard. Total: 480 TND impayés.', type: 'ALERTE', lu: false, createdAt: '2026-09-15T08:00:00' },
  { id: 2,  titre: 'Certificats médicaux manquants', message: '3 joueurs n\'ont pas encore fourni leur certificat médical (Zied, Ibrahim, Skander).', type: 'ALERTE', lu: false, createdAt: '2026-09-14T09:00:00' },
  { id: 3,  titre: 'Nouveau joueur inscrit',      message: 'Ibrahim Ben Salem a été inscrit en U9 — catégorie Formation.', type: 'INFO', lu: false, createdAt: '2026-09-12T14:30:00' },
  { id: 4,  titre: 'Autorisations parentales manquantes', message: '3 joueurs n\'ont pas d\'autorisation parentale (Zied, Mehdi, Marouen).', type: 'ALERTE', lu: true, createdAt: '2026-09-13T09:00:00' },
  { id: 5,  titre: 'Tournoi Inter-Académies',     message: 'Inscription confirmée pour le Tournoi Inter-Académies les 4-5 octobre à El Menzah.', type: 'INFO', lu: true, createdAt: '2026-09-10T10:00:00' },
  { id: 6,  titre: 'Paiement reçu',               message: 'Paiement de 360 TND reçu de Hamza Bouazizi (SEMESTRIEL).', type: 'SUCCES', lu: true, createdAt: '2026-09-14T14:00:00' },
  { id: 7,  titre: 'Match Amical confirmé',        message: 'Match amical Nadi vs Espérance U17 le 27 septembre à 15h.', type: 'INFO', lu: true, createdAt: '2026-09-08T11:00:00' },
  { id: 8,  titre: 'Consentement RGPD manquant',   message: 'Le consentement RGPD de Fathi Bouazizi et Fatma Zouari n\'est pas enregistré.', type: 'ALERTE', lu: true, createdAt: '2026-09-05T09:00:00' },
];

// ─── Audit Logs ──────────────────────────────────────
export const DEMO_AUDIT_LOGS = [
  { id: 1,  action: 'LOGIN',  entite: 'USER',     entiteId: 1,  details: 'Connexion réussie',                                              utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-15T08:00:00' },
  { id: 2,  action: 'CREATE', entite: 'JOUEUR',   entiteId: 7,  details: 'Joueur Ibrahim Ben Salem créé — U9',                               utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-12T14:30:00' },
  { id: 3,  action: 'UPDATE', entite: 'JOUEUR',   entiteId: 9,  details: 'Fréquence de paiement modifiée: TRIMESTRIEL → MENSUEL',           utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-11T10:00:00' },
  { id: 4,  action: 'CREATE', entite: 'PAIEMENT', entiteId: 11, details: 'Paiement de 360 TND enregistré — Hamza Bouazizi (SEMESTRIEL)',    utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-14T14:00:00' },
  { id: 5,  action: 'CREATE', entite: 'EVENEMENT',entiteId: 1,  details: 'Événement créé: Tournoi Inter-Académies Tunis (4-5 octobre)',     utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-10T10:00:00' },
  { id: 6,  action: 'UPDATE', entite: 'SLOT',     entiteId: 7,  details: 'Créneau modifié: U13 Jeudi 19:00 — ajout terrain Salle omnisports', utilisateurEmail: 'admin@nadi.tn',    timestamp: '2026-09-09T11:00:00' },
  { id: 7,  action: 'DELETE', entite: 'SLOT',     entiteId: 17, details: 'Créneau supprimé: U15 Samedi 10:00 (conflit horaire)',             utilisateurEmail: 'admin@nadi.tn',       timestamp: '2026-09-08T09:00:00' },
  { id: 8,  action: 'LOGIN',  entite: 'USER',     entiteId: 2,  details: 'Connexion教练',                                                    utilisateurEmail: 'coach@nadi.tn',       timestamp: '2026-09-15T07:30:00' },
  { id: 9,  action: 'UPDATE', entite: 'PRESENCE', entiteId: 1,  details: 'Présence enregistrée: Lundi U9 2026-09-15 — 14 présents / 16',   utilisateurEmail: 'coach@nadi.tn',        timestamp: '2026-09-15T17:30:00' },
  { id: 10, action: 'LOGIN',  entite: 'USER',     entiteId: 3,  details: 'Connexion parent',                                                  utilisateurEmail: 'parent@nadi.tn',      timestamp: '2026-09-14T20:00:00' },
];
