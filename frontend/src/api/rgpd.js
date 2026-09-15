import api from './client';

export const rgpdApi = {
  getConsentRegister: () => api.get('/rgpd/consent-register'),

  getByParent: (parentId) => api.get(`/rgpd/consent-register/parent/${parentId}`),

  record: (data) => api.post('/rgpd/consent-register', data),

  exportCsv: () => api.get('/rgpd/consent-register/export/csv', { responseType: 'blob' }),
};
