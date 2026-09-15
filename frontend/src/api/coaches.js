import api from './client';

export const coachesApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 20, search } = params;
    const query = new URLSearchParams({ page, size });
    if (search) query.append('search', search);
    return api.get(`/coaches?${query}`);
  },

  getList: () => api.get('/coaches/list'),

  getById: (id) => api.get(`/coaches/${id}`),

  create: (data) => api.post('/coaches', data),

  update: (id, data) => api.put(`/coaches/${id}`, data),

  delete: (id) => api.delete(`/coaches/${id}`),
};
