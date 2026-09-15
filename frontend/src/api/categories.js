import api from './client';

export const categoriesApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 50, search } = params;
    const query = new URLSearchParams({ page, size });
    if (search) query.append('search', search);
    return api.get(`/categories?${query}`);
  },

  getById: (id) => api.get(`/categories/${id}`),

  create: (data) => api.post('/categories', data),

  update: (id, data) => api.put(`/categories/${id}`, data),

  delete: (id) => api.delete(`/categories/${id}`),
};
