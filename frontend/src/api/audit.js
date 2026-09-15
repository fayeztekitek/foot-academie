import api from './client';

export const auditApi = {
  getAll: (params = {}) => {
    const { page = 0, size = 50 } = params;
    return api.get(`/audit?page=${page}&size=${size}`);
  },

  getByUser: (email, params = {}) => {
    const { page = 0, size = 50 } = params;
    return api.get(`/audit/user/${email}?page=${page}&size=${size}`);
  },
};
