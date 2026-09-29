import api from './client';

export const calendarApi = {
  getToken: () => api.get('/calendar/token').then(r => r.data),

  regenerate: () => api.post('/calendar/token').then(r => r.data),
};
