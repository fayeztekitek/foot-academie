import api from './client';

export const notesApi = {
  getByJoueur: (joueurId, params = {}) => {
    const { page = 0, size = 20 } = params;
    const query = new URLSearchParams({ page, size });
    return api.get(`/notes/joueur/${joueurId}?${query}`);
  },

  create: (data) => api.post('/notes', data),

  getStats: (joueurId) => api.get(`/notes/joueur/${joueurId}/stats`),

  getJoueursDuMois: (mois, annee) => api.get(`/notes/joueur-du-mois?mois=${mois}&annee=${annee}`),
};
