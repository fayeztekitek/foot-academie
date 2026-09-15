import api from './client';

export const slotsApi = {
  getAll: (params = {}) => {
    const query = new URLSearchParams();
    if (params.jour) query.append('jour', params.jour);
    if (params.categorieId) query.append('categorieId', params.categorieId);
    if (params.entraineurId) query.append('entraineurId', params.entraineurId);
    const qs = query.toString();
    return api.get(`/slots${qs ? '?' + qs : ''}`);
  },

  create: (data) => api.post('/slots', data),

  update: (id, data) => api.put(`/slots/${id}`, data),

  delete: (id) => api.delete(`/slots/${id}`),
};
