import api from './client';

export const parentsApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 20, search } = params;
    const query = new URLSearchParams({ page, size });
    if (search) query.append('search', search);
    return api.get(`/parents?${query}`);
  },

  getById: (id) => api.get(`/parents/${id}`),

  getMe: () => api.get('/parents/me'),

  create: (data) => api.post('/parents', data),

  update: (id, data) => api.put(`/parents/${id}`, data),

  delete: (id) => api.delete(`/parents/${id}`),

  resetPassword: (id, motDePasse) => api.post(`/parents/${id}/reset-password`, { motDePasse }),
};
