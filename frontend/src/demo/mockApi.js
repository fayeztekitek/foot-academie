import {
  DEMO_CATEGORIES, DEMO_COACHES, DEMO_PARENTS, DEMO_PLAYERS,
  DEMO_PAYMENTS, DEMO_SLOTS, DEMO_NOTIFICATIONS, DEMO_AUDIT_LOGS,
  DEMO_ATTENDANCE_STATS, DEMO_EVENTS, DEMO_CONVOCATIONS,
} from './mockData';

const DB = {
  categories: [...DEMO_CATEGORIES],
  coaches: [...DEMO_COACHES],
  parents: [...DEMO_PARENTS],
  players: [...DEMO_PLAYERS],
  payments: [...DEMO_PAYMENTS],
  slots: [...DEMO_SLOTS],
  notifications: [...DEMO_NOTIFICATIONS],
  auditLogs: [...DEMO_AUDIT_LOGS],
  attendanceStats: [...DEMO_ATTENDANCE_STATS],
  events: [...DEMO_EVENTS],
  convocations: [...DEMO_CONVOCATIONS],
};

let nextId = 300;
const genId = () => ++nextId;

function paginate(arr, page = 0, size = 20) {
  const start = page * size;
  return {
    content: arr.slice(start, start + size),
    totalElements: arr.length,
    totalPages: Math.ceil(arr.length / size),
    number: page,
    size,
  };
}

function search(arr, q, fields) {
  if (!q) return arr;
  const lower = q.toLowerCase();
  return arr.filter(item => fields.some(f => String(item[f] || '').toLowerCase().includes(lower)));
}

export const MOCK_API = {
  // ─── Auth ────────────────────────────────────────
  '/auth/login': {
    POST: (body) => {
      const accounts = {
        'superadmin@nadi.tn': { role: 'SUPER_ADMIN', password: 'superadmin123', email: 'superadmin@nadi.tn' },
        'admin@nadi.tn': { role: 'ADMIN', password: 'admin123', email: 'admin@nadi.tn' },
        'coach@nadi.tn': { role: 'COACH', password: 'coach123', email: 'coach@nadi.tn' },
        'parent@nadi.tn': { role: 'PARENT', password: 'parent123', email: 'parent@nadi.tn' },
      };
      const account = accounts[body.email];
      if (!account || account.password !== body.motDePasse) {
        throw { status: 401, message: 'Identifiants incorrects' };
      }
      return {
        data: {
          accessToken: 'demo-token-' + account.role.toLowerCase(),
          refreshToken: 'demo-refresh-' + account.role.toLowerCase(),
          role: account.role,
          email: account.email,
        }
      };
    }
  },

  '/auth/change-password': {
    POST: () => ({ data: { message: 'Mot de passe modifié avec succès' } })
  },

  '/auth/tenants': {
    GET: () => ({
      data: [{ id: 1, slug: 'nadi-default', nom: 'Nadi Académie', ville: 'Tunis', logoUrl: null }]
    })
  },

  // ─── Dashboard ──────────────────────────────────
  '/dashboard/stats': {
    GET: () => {
      const totalPlayers = DB.players.length;
      const activePlayers = DB.players.filter(p => p.statutPaiement === 'A_JOUR').length;
      const pendingPayments = DB.payments.filter(p => p.statut === 'EN_ATTENTE' || p.statut === 'EN_RETARD').length;
      const overduePayments = DB.payments.filter(p => p.statut === 'EN_RETARD').length;
      const complianceAlerts = DB.parents.filter(p => !p.consentementRGPD).length
        + DB.players.filter(p => !p.certificatMedical).length
        + DB.players.filter(p => !p.autorisationParentale).length;
      const totalRevenue = DB.payments.filter(p => p.statut === 'PAYE').reduce((sum, p) => sum + p.montant, 0);
      const joueursSansCertificatMedical = DB.players.filter(p => !p.certificatMedical).length;
      const joueursSansAutorisationParentale = DB.players.filter(p => !p.autorisationParentale).length;
      const expiredDocuments = 0;
      const expiringDocuments = 2;
      const parentsImpayes2Mois = DB.players.filter(p => p.moisImpayes >= 2).length;
      return {
        data: {
          totalPlayers, activePlayers, pendingPayments: overduePayments,
          complianceAlerts, totalRevenue, joueursSansCertificatMedical,
          joueursSansAutorisationParentale, expiredDocuments, expiringDocuments,
          parentsImpayes2Mois,
        }
      };
    }
  },

  '/stats/detailed': {
    GET: () => {
      const byCat = {};
      DB.players.forEach(p => {
        byCat[p.categorieNom] = (byCat[p.categorieNom] || 0) + 1;
      });
      const months = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'];
      const revenueHistory = months.map((m, i) => ({
        month: m,
        revenue: DB.payments.filter(p => p.statut === 'PAYE' && p.datePaiement && new Date(p.datePaiement).getMonth() === i).reduce((s, p) => s + p.montant, 0)
      }));
      const attendanceSummary = { present: 85, absence: 10, retard: 5 };
      return { data: { playersByCategory: byCat, revenueHistory, attendanceSummary } };
    }
  },

  // ─── Players ────────────────────────────────────
  '/players': {
    GET: (params) => {
      let list = [...DB.players];
      if (params.search) list = search(list, params.search, ['prenom', 'nom']);
      if (params.categorieId) list = list.filter(p => p.categorieId === Number(params.categorieId));
      if (params.parentId) list = list.filter(p => p.parentId === Number(params.parentId));
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 20)) };
    },
    POST: (body) => {
      const cat = DB.categories.find(c => c.id === Number(body.categorieId));
      const parent = DB.parents.find(p => p.id === Number(body.parentId));
      const player = {
        id: genId(), prenom: body.prenom, nom: body.nom,
        dateNaissance: body.dateNaissance || null, dateEntree: body.dateEntree || null,
        categorieId: Number(body.categorieId), categorieNom: cat?.nom || 'U9',
        parentId: Number(body.parentId), parentPrenom: parent?.prenom || '', parentNom: parent?.nom || '',
        statutPaiement: 'EN_ATTENTE', frequence: body.frequence || 'MENSUEL',
        moisAVerser: 0, moisPayes: 0, moisImpayes: 0,
        certificatMedical: body.certificatMedical || false,
        autorisationParentale: body.autorisationParentale || false,
      };
      DB.players.push(player);
      return { data: player };
    }
  },

  '/players/': {
    GET: (id) => {
      const player = DB.players.find(p => p.id === Number(id));
      if (!player) throw { status: 404, message: 'Joueur non trouvé' };
      return { data: player };
    },
    PUT: (id, body) => {
      const idx = DB.players.findIndex(p => p.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Joueur non trouvé' };
      const cat = DB.categories.find(c => c.id === Number(body.categorieId || DB.players[idx].categorieId));
      const parent = DB.parents.find(p => p.id === Number(body.parentId || DB.players[idx].parentId));
      DB.players[idx] = {
        ...DB.players[idx],
        prenom: body.prenom || DB.players[idx].prenom,
        nom: body.nom || DB.players[idx].nom,
        dateNaissance: body.dateNaissance || DB.players[idx].dateNaissance,
        dateEntree: body.dateEntree || DB.players[idx].dateEntree,
        categorieId: Number(body.categorieId || DB.players[idx].categorieId),
        categorieNom: cat?.nom || DB.players[idx].categorieNom,
        parentId: Number(body.parentId || DB.players[idx].parentId),
        parentPrenom: parent?.prenom || DB.players[idx].parentPrenom,
        parentNom: parent?.nom || DB.players[idx].parentNom,
        frequence: body.frequence || DB.players[idx].frequence,
        certificatMedical: body.certificatMedical !== undefined ? body.certificatMedical : DB.players[idx].certificatMedical,
        autorisationParentale: body.autorisationParentale !== undefined ? body.autorisationParentale : DB.players[idx].autorisationParentale,
      };
      return { data: DB.players[idx] };
    },
    DELETE: (id) => {
      const idx = DB.players.findIndex(p => p.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Joueur non trouvé' };
      DB.players.splice(idx, 1);
      return { data: { message: 'Joueur supprimé' } };
    }
  },

  '/players/change-frequency': {
    POST: (body) => {
      const idx = DB.players.findIndex(p => p.id === Number(body.id || body.joueurId));
      if (idx === -1) throw { status: 404, message: 'Joueur non trouvé' };
      DB.players[idx].frequence = body.frequence;
      return { data: DB.players[idx] };
    }
  },

  // ─── Parents ────────────────────────────────────
  '/parents': {
    GET: (params) => {
      let list = [...DB.parents];
      if (params.search) list = search(list, params.search, ['prenom', 'nom', 'email']);
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 20)) };
    },
    POST: (body) => {
      const parent = {
        id: genId(), prenom: body.prenom, nom: body.nom,
        telephone: body.telephone || '', email: body.email || '',
        consentementRGPD: body.consentementRGPD || false, joueurs: [],
      };
      DB.parents.push(parent);
      return { data: parent };
    }
  },

  '/parents/me': {
    GET: () => ({ data: DB.parents[0] || {} })
  },

  '/parents/': {
    PUT: (id, body) => {
      const idx = DB.parents.findIndex(p => p.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Parent non trouvé' };
      DB.parents[idx] = { ...DB.parents[idx], ...body, id: DB.parents[idx].id };
      return { data: DB.parents[idx] };
    },
    DELETE: (id) => {
      const idx = DB.parents.findIndex(p => p.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Parent non trouvé' };
      DB.parents.splice(idx, 1);
      return { data: { message: 'Parent supprimé' } };
    }
  },

  // ─── Coaches ────────────────────────────────────
  '/coaches': {
    GET: (params) => {
      let list = [...DB.coaches];
      if (params.search) list = search(list, params.search, ['prenom', 'nom', 'specialite']);
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 20)) };
    },
    POST: (body) => {
      const catIds = (body.categorieIds || []).map(Number);
      const cats = DB.categories.filter(c => catIds.includes(c.id)).map(c => c.nom);
      const coach = {
        id: genId(), prenom: body.prenom, nom: body.nom,
        specialite: body.specialite || '', telephone: body.telephone || '',
        email: body.email || '', categorieIds: catIds, categories: cats,
      };
      DB.coaches.push(coach);
      return { data: coach };
    }
  },

  '/coaches/list': {
    GET: () => ({ data: DB.coaches.map(c => ({ id: c.id, prenom: c.prenom, nom: c.nom })) })
  },

  '/coaches/': {
    PUT: (id, body) => {
      const idx = DB.coaches.findIndex(c => c.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Entraîneur non trouvé' };
      const catIds = (body.categorieIds || DB.coaches[idx].categorieIds || []).map(Number);
      const cats = DB.categories.filter(c => catIds.includes(c.id)).map(c => c.nom);
      DB.coaches[idx] = { ...DB.coaches[idx], ...body, categorieIds: catIds, categories: cats, id: DB.coaches[idx].id };
      return { data: DB.coaches[idx] };
    },
    DELETE: (id) => {
      const idx = DB.coaches.findIndex(c => c.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Entraîneur non trouvé' };
      DB.coaches.splice(idx, 1);
      return { data: { message: 'Entraîneur supprimé' } };
    }
  },

  // ─── Categories ─────────────────────────────────
  '/categories': {
    GET: (params) => {
      let list = [...DB.categories];
      if (params.search) list = search(list, params.search, ['nom', 'description']);
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 50)) };
    },
    POST: (body) => {
      const cat = { id: genId(), nom: body.nom, description: body.description || '', maxJoueurs: body.maxJoueurs || 20, actif: true };
      DB.categories.push(cat);
      return { data: cat };
    }
  },

  '/categories/': {
    PUT: (id, body) => {
      const idx = DB.categories.findIndex(c => c.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Catégorie non trouvée' };
      DB.categories[idx] = { ...DB.categories[idx], ...body, id: DB.categories[idx].id };
      return { data: DB.categories[idx] };
    },
    DELETE: (id) => {
      const idx = DB.categories.findIndex(c => c.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Catégorie non trouvée' };
      DB.categories.splice(idx, 1);
      return { data: { message: 'Catégorie supprimée' } };
    }
  },

  // ─── Slots ──────────────────────────────────────
  '/slots': {
    GET: (params) => {
      let list = [...DB.slots];
      if (params.jour) list = list.filter(s => s.jourSemaine === params.jour);
      if (params.categorieId) list = list.filter(s => s.categorieId === Number(params.categorieId));
      return { data: list };
    },
    POST: (body) => {
      const cat = DB.categories.find(c => c.id === Number(body.categorieId));
      const coach = DB.coaches.find(c => c.id === Number(body.entraineurId));
      const slot = {
        id: genId(), jourSemaine: body.jourSemaine || body.jour, heureDebut: body.heureDebut, heureFin: body.heureFin,
        categorieId: Number(body.categorieId), categorieNom: cat?.nom || '',
        entraineurId: Number(body.entraineurId), entraineurPrenom: coach?.prenom || '', entraineurNom: coach?.nom || '',
        terrain: body.terrain || 'Terrain A', capacite: body.capacite || 20,
      };
      DB.slots.push(slot);
      return { data: slot };
    }
  },

  '/slots/': {
    PUT: (id, body) => {
      const idx = DB.slots.findIndex(s => s.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Créneau non trouvé' };
      DB.slots[idx] = { ...DB.slots[idx], ...body, id: DB.slots[idx].id };
      return { data: DB.slots[idx] };
    },
    DELETE: (id) => {
      const idx = DB.slots.findIndex(s => s.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Créneau non trouvé' };
      DB.slots.splice(idx, 1);
      return { data: { message: 'Créneau supprimé' } };
    }
  },

  // ─── Payments ───────────────────────────────────
  '/payments': {
    GET: (params) => {
      let list = [...DB.payments];
      if (params.statut) list = list.filter(p => p.statut === params.statut);
      if (params.joueurId) list = list.filter(p => p.joueurId === Number(params.joueurId));
      if (params.parentId) list = list.filter(p => p.parentId === Number(params.parentId));
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 50)) };
    },
    POST: (body) => {
      const joueur = DB.players.find(p => p.id === Number(body.joueurId));
      const payment = {
        id: genId(), joueurId: Number(body.joueurId),
        joueurPrenom: joueur?.prenom || '', joueurNom: joueur?.nom || '',
        parentId: joueur?.parentId || 0, parentPrenom: joueur?.parentPrenom || '', parentNom: joueur?.parentNom || '',
        montant: body.montant || 0, devise: 'TND',
        dateEcheance: body.dateEcheance || null, datePaiement: null, dateEncaissement: null,
        statut: 'EN_ATTENTE', moyenPaiement: null, formule: body.formule || 'MENSUEL',
        frequence: body.frequence || 'MENSUEL', reference: null, numeroRecu: null,
        commentaire: body.commentaire || '', notes: body.notes || '',
        enregistreParId: 1, enregistreParEmail: 'admin@nadi.tn',
        createdAt: new Date().toISOString(),
      };
      DB.payments.push(payment);
      return { data: payment };
    }
  },

  '/payments/overdue': {
    GET: () => ({ data: DB.payments.filter(p => p.statut === 'EN_RETARD') })
  },

  '/payments/mark-paid': {
    POST: (idOrBody, bodyMaybe) => {
      const id = typeof idOrBody === 'number' || typeof idOrBody === 'string' ? idOrBody : idOrBody?.id;
      const b = bodyMaybe || idOrBody;
      const idx = DB.payments.findIndex(p => p.id === Number(id));
      if (idx === -1) throw { status: 404, message: 'Paiement non trouvé' };
      DB.payments[idx] = {
        ...DB.payments[idx],
        statut: 'PAYE',
        datePaiement: b?.datePaiement || new Date().toISOString().split('T')[0],
        dateEncaissement: b?.datePaiement || new Date().toISOString().split('T')[0],
        moyenPaiement: b?.moyenPaiement || 'ESPECES',
        reference: 'PAI-' + Date.now(),
        numeroRecu: 'REC-' + Date.now(),
      };
      return { data: DB.payments[idx] };
    }
  },

  // ─── Presences / Attendance ─────────────────────
  '/presences/global-stats': {
    GET: () => {
      const mois = { attendanceRate: 82, presentCount: 156, totalSessions: 190 };
      const trimestre = { attendanceRate: 79, presentCount: 465, totalSessions: 590 };
      const semestre = { attendanceRate: 77, presentCount: 920, totalSessions: 1195 };
      const annee = { attendanceRate: 77, presentCount: 920, totalSessions: 1195 };
      return { data: { mois, trimestre, semestre, annee } };
    }
  },

  '/presences/all-joueur-stats': {
    GET: () => ({ data: DB.attendanceStats })
  },

  '/presences/my-children-stats': {
    GET: () => {
      const parentPlayers = DB.attendanceStats.filter(s => {
        const player = DB.players.find(p => p.id === s.joueurId);
        return player && player.parentId === 1;
      });
      return { data: parentPlayers };
    }
  },

  // ─── Events ─────────────────────────────────────
  '/events': {
    GET: () => ({ data: DB.events }),
    POST: (body) => {
      const event = { id: genId(), ...body, joueurIds: body.joueurIds || [], categorieIds: body.categorieIds || [] };
      DB.events.push(event);
      return { data: event };
    }
  },

  '/events/convocations': {
    GET: (params) => {
      const eventId = params?.evenementId || params?.eventId;
      if (eventId) {
        return { data: DB.convocations.filter(c => c.evenementId === Number(eventId)) };
      }
      return { data: DB.convocations };
    }
  },

  '/events/my-convocations': {
    GET: () => {
      const parentPlayerIds = DB.players.filter(p => p.parentId === 1).map(p => p.id);
      return { data: DB.convocations.filter(c => parentPlayerIds.includes(c.joueurId)) };
    }
  },

  // ─── Notifications ──────────────────────────────
  '/notifications': {
    GET: (params) => {
      const list = [...DB.notifications].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 20)) };
    }
  },

  '/notifications/unread': {
    GET: () => ({ data: DB.notifications.filter(n => !n.lu).length })
  },

  // ─── RGPD ───────────────────────────────────────
  '/rgpd/consents': {
    GET: () => {
      const consents = DB.parents.map((p, i) => ({
        id: i + 1, parentId: p.id, parentPrenom: p.prenom, parentNom: p.nom,
        type: 'TRAITEMENT_DONNEES_PERSONNELLES',
        accord: p.consentementRGPD,
        dateConsentement: '2026-09-01T10:00:00',
        source: 'INSCRIPTION',
        exported: false,
      }));
      return { data: paginate(consents, 0, 50) };
    },
    POST: (body) => ({ data: { id: genId(), ...body, dateConsentement: new Date().toISOString() } })
  },

  '/rgpd/consents/stats': {
    GET: () => ({
      data: {
        total: DB.parents.length,
        granted: DB.parents.filter(p => p.consentementRGPD).length,
        refused: DB.parents.filter(p => !p.consentementRGPD).length,
        byType: { TRAITEMENT_DONNEES_PERSONNELLES: DB.parents.length },
      }
    })
  },

  // ─── Billing ────────────────────────────────────
  '/billing/abonnement': {
    GET: () => ({
      data: {
        plan: 'PRO', statut: 'ACTIF', montantMensuel: 99, devise: 'TND',
        maxJoueurs: 100, maxCoachs: 10, maxParents: 500,
        joueursUtilises: DB.players.length, coachsUtilises: DB.coaches.length, parentsUtilises: DB.parents.length,
        joueursLimitReached: false, coachsLimitReached: false, parentsLimitReached: false,
      }
    })
  },
  '/billing/factures': {
    GET: () => ({
      data: [
        { id: 1, reference: 'FAC-2026-009', date: '2026-09-01', montant: 99, statut: 'PAYEE', description: 'Abonnement PRO — Septembre 2026' },
        { id: 2, reference: 'FAC-2026-008', date: '2026-08-01', montant: 99, statut: 'PAYEE', description: 'Abonnement PRO — Août 2026' },
        { id: 3, reference: 'FAC-2026-007', date: '2026-07-01', montant: 99, statut: 'PAYEE', description: 'Abonnement PRO — Juillet 2026' },
      ]
    })
  },
  '/billing/plans': {
    GET: () => ({
      data: {
        plans: {
          FREE: { label: 'Gratuit', maxJoueurs: 10, maxCoachs: 2, maxParents: 20, prix: '0 TND/mois' },
          PRO: { label: 'Pro', maxJoueurs: 100, maxCoachs: 10, maxParents: 500, prix: '99 TND/mois' },
          PREMIUM: { label: 'Premium', maxJoueurs: 999, maxCoachs: 99, maxParents: 9999, prix: '249 TND/mois' },
        }
      }
    })
  },
  '/billing/subscribe': { POST: (body) => ({ data: { plan: body.plan, statut: 'ACTIF' } }) },

  // ─── Academies ──────────────────────────────────
  '/academies': {
    GET: () => ({
      data: {
        content: [
          { id: 1, slug: 'nadi-default', nom: 'Nadi Académie', ville: 'Tunis', active: true, plan: 'PRO', joueurCount: 20, parentCount: 10, coachCount: 5, email: 'contact@nadi.tn', telephone: '+216 71 000 000', createdAt: '2026-09-01T00:00:00' },
        ],
        totalElements: 1,
      }
    }),
    POST: (body) => {
      const a = { id: genId(), ...body, active: true, joueurCount: 0, parentCount: 0, coachCount: 0, createdAt: new Date().toISOString() };
      return { data: a };
    }
  },

  // ─── Invitations ────────────────────────────────
  '/invitations': {
    GET: () => ({
      data: {
        content: [
          { id: 1, email: 'nouveau.coach@nadi.tn', role: 'COACH', invitedByEmail: 'admin@nadi.tn', statut: 'EN_ATTENTE', dateExpiration: '2026-09-22T00:00:00', createdAt: '2026-09-15T10:00:00' },
          { id: 2, email: 'nouveau.parent@nadi.tn', role: 'PARENT', invitedByEmail: 'admin@nadi.tn', statut: 'ACCEPTEE', dateExpiration: '2026-09-20T00:00:00', createdAt: '2026-09-13T14:00:00' },
        ],
        totalElements: 2,
      }
    }),
    POST: (body) => {
      const inv = { id: genId(), token: 'mock-token-' + genId(), email: body.email, role: body.role, invitedByEmail: 'admin@nadi.tn', statut: 'EN_ATTENTE', dateExpiration: new Date(Date.now() + 7 * 86400000).toISOString(), createdAt: new Date().toISOString() };
      return { data: inv };
    }
  },

  // ─── Super Admin Stats ─────────────────────────
  '/stats/super-admin': {
    GET: () => ({
      data: {
        totalAcademies: 1, activeAcademies: 1, inactiveAcademies: 0,
        totalUsers: 16, totalPlayers: 20, totalParents: 10, totalCoaches: 5,
        totalPayments: DB.payments.length, totalRevenue: DB.payments.filter(p => p.statut === 'PAYE').reduce((s, p) => s + p.montant, 0),
        academies: [
          { id: 1, nom: 'Nadi Académie', slug: 'nadi-default', ville: 'Tunis', plan: 'PRO', active: true, joueurCount: 20, parentCount: 10, coachCount: 5, paymentCount: DB.payments.length, revenue: DB.payments.filter(p => p.statut === 'PAYE').reduce((s, p) => s + p.montant, 0) },
        ],
        byPlan: [{ plan: 'PRO', count: 1 }],
      }
    })
  },

  // ─── Onboarding ─────────────────────────────────
  '/onboarding': {
    POST: (body) => ({
      data: { academieId: genId(), academieSlug: 'demo-academy', academieNom: body.academieNom, adminEmail: body.adminEmail, message: 'Académie créée avec succès' }
    })
  },

  // ─── Reports ────────────────────────────────────
  '/reports/payments': {
    GET: () => {
      const csv = 'Joueur,Parent,Montant,Statut,Date\n' + DB.payments.map(p => `${p.joueurPrenom} ${p.joueurNom},${p.parentPrenom} ${p.parentNom},${p.montant} TND,${p.statut},${p.dateEcheance || ''}`).join('\n');
      return { data: csv, headers: { 'content-type': 'text/csv' } };
    }
  },
  '/reports/payments/excel': {
    GET: () => {
      const rows = DB.payments.map(p =>
        `<Row><Cell><Data ss:Type="String">${p.joueurPrenom} ${p.joueurNom}</Data></Cell><Cell><Data ss:Type="String">${p.parentPrenom} ${p.parentNom}</Data></Cell><Cell><Data ss:Type="String">${p.montant} TND</Data></Cell><Cell><Data ss:Type="String">${p.statut}</Data></Cell><Cell><Data ss:Type="String">${p.dateEcheance || ''}</Data></Cell></Row>`
      ).join('\n');
      const xml = `<?xml version="1.0"?><?mso-application progid="Excel.Sheet"?><Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"><Worksheet ss:Name="Paiements"><Table><Row><Cell><Data ss:Type="String">Joueur</Data></Cell><Cell><Data ss:Type="String">Parent</Data></Cell><Cell><Data ss:Type="String">Montant</Data></Cell><Cell><Data ss:Type="String">Statut</Data></Cell><Cell><Data ss:Type="String">Date</Data></Cell></Row>${rows}</Table></Worksheet></Workbook>`;
      return { data: xml, headers: { 'content-type': 'application/vnd.ms-excel' } };
    }
  },

  // ─── Audit ──────────────────────────────────────
  '/audit': {
    GET: (params) => {
      const list = [...DB.auditLogs].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      return { data: paginate(list, Number(params.page || 0), Number(params.size || 20)) };
    }
  },

  // ─── Change Password ────────────────────────────
  '/parents/reset-password': {
    POST: () => ({ data: { message: 'Mot de passe réinitialisé' } })
  },
};

export function handleMockRequest(method, url, body, params) {
  let normalizedUrl = url.replace(/\/\d+(\/|$)/g, '/');

  // Handle specific patterns with IDs
  const idMatch = url.match(/\/(\d+)(\/|$)/);
  const numericId = idMatch ? idMatch[1] : null;

  const tryHandler = (handler, withId) => {
    try {
      if (withId && numericId) {
        return handler(numericId, body || params);
      }
      return handler(body || params);
    } catch (err) {
      return Promise.reject(err);
    }
  };

  // Pass 1: exact matches on every pattern first, so that a longer route
  // (e.g. /reports/payments/excel) is never shadowed by a shorter prefix
  // (e.g. /reports/payments) declared earlier in the table.
  for (const [pattern, handlers] of Object.entries(MOCK_API)) {
    const handler = handlers[method];
    if (!handler) continue;

    if (pattern === normalizedUrl || pattern === url) {
      return tryHandler(handler, true);
    }
  }

  // Pass 2: prefix matches (for routes like /players/ with IDs)
  for (const [pattern, handlers] of Object.entries(MOCK_API)) {
    const handler = handlers[method];
    if (!handler) continue;

    const cleanPattern = pattern.replace(/\/$/, '');
    if (url.startsWith(cleanPattern + '/') || url.startsWith(cleanPattern + '?')) {
      return tryHandler(handler, true);
    }
  }

  return Promise.reject({ status: 404, message: `Route non trouvée: ${method} ${url}` });
}
