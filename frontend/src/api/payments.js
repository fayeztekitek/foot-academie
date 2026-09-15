import api from './client';

export const paymentsApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 20, statut, joueurId, parentId } = params;
    const query = new URLSearchParams({ page, size });
    if (statut) query.append('statut', statut);
    if (joueurId) query.append('joueurId', joueurId);
    if (parentId) query.append('parentId', parentId);
    return api.get(`/payments?${query}`);
  },

  getOverdue: () => api.get('/payments/overdue'),

  getByMonth: (year, month) => api.get(`/payments/month/${year}/${month}`),

  create: (data) => api.post('/payments', data),

  update: (id, data) => api.patch(`/payments/${id}`, data),

  markAsPaid: (id, data) => api.post(`/payments/${id}/pay`, data || {}),

  generateSchedule: (data) => api.post('/payments/generate-schedule', data),

  delete: (id) => api.delete(`/payments/${id}`),
};
