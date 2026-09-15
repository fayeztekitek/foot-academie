import api from './client';

export const playersApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 20, search, categorieId, parentId } = params;
    const query = new URLSearchParams({ page, size });
    if (search) query.append('search', search);
    if (categorieId) query.append('categorieId', categorieId);
    if (parentId) query.append('parentId', parentId);
    return api.get(`/players?${query}`);
  },

  getById: (id) => api.get(`/players/${id}`),

  create: (data) => api.post('/players', data),

  update: (id, data) => api.put(`/players/${id}`, data),

  delete: (id) => api.delete(`/players/${id}`),

  changeFrequency: (id, frequence) => api.patch(`/players/${id}/frequence-paiement`, { frequence }),
};
