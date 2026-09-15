import api from './client';

export const notificationsApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 20 } = params;
    return api.get(`/notifications?page=${page}&size=${size}`);
  },

  getUnreadCount: () => api.get('/notifications/unread'),

  markAsRead: (id) => api.post(`/notifications/${id}/read`),

  markAllAsRead: () => api.post('/notifications/read-all'),
};
