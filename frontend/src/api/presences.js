import api from './client';

export const presencesApi = {
  getByCreneauAndDate: (creneauId, date) =>
    api.get(`/presences?creneauId=${creneauId}&date=${date}`),

  save: (data) => api.post('/presences', data),

  declare: (data) => api.post('/presences/declarer', data),

  getMyAbsences: () => api.get('/presences/mes-absences'),

  getGlobalStats: () => api.get('/attendance/global'),

  getAllJoueurStats: () => api.get('/attendance/all'),

  getJoueurStats: (joueurId) => api.get(`/attendance/joueur/${joueurId}`),

  getMyChildrenStats: () => api.get('/attendance/my-children'),
};
