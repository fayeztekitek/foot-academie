import api from './client';

export const documentsApi = {
  getByJoueur: (joueurId) => api.get(`/documents/joueur/${joueurId}`),

  getByParent: (parentId) => api.get(`/documents/parent/${parentId}`),

  getExpiring: (days = 30) => api.get(`/documents/expiring?days=${days}`),

  getExpired: () => api.get('/documents/expired'),

  create: (data) => api.post('/documents', data),

  upload: (joueurId, type, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(
      `/documents/upload?joueurId=${encodeURIComponent(joueurId)}&type=${encodeURIComponent(type)}`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },

  updateStatut: (id, statut) => api.put(`/documents/${id}/statut`, { statut }),
};
