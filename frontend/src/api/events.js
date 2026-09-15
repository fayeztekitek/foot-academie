import api from './client';

export const eventsApi = {
  getAll: () => api.get('/events'),
  getByMonth: (year, month) => api.get(`/events/month/${year}/${month}`),
  create: (data) => api.post('/events', data),
  update: (id, data) => api.patch(`/events/${id}`, data),
  delete: (id) => api.delete(`/events/${id}`),
  getConvocations: (eventId) => api.get(`/events/${eventId}/convocations`),
  getMyConvocations: () => api.get('/events/my-convocations'),
  respond: (convocationId, accept) => api.post(`/events/convocations/${convocationId}/respond`, { accept }),
};
