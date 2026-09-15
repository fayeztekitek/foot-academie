import api from './client';

export const statsApi = {
  getDetailed: () => api.get('/stats/detailed'),

  exportCsv: () => api.get('/stats/export/csv', { responseType: 'blob' }),
};
