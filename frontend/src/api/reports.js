import api from './client';

export const reportsApi = {
  exportCsv: (start, end) => {
    const query = new URLSearchParams({ start, end });
    return api.get(`/reports/payments?${query}`, { responseType: 'blob' });
  },

  exportExcel: (start, end) => {
    const query = new URLSearchParams({ start, end });
    return api.get(`/reports/payments/excel?${query}`, { responseType: 'blob' });
  },
};
