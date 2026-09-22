import api from './client';

export const rgpdApi = {
  getConsents: () => api.get('/rgpd/consents'),

  getByParent: (parentId) => api.get(`/rgpd/consents`, { params: { parentId } }),

  record: (data) => api.post('/rgpd/consents', data),

  exportCsv: () => api.get('/rgpd/consent-register/export/csv', { responseType: 'blob' }),
};
