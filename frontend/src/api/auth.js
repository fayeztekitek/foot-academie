import api from './client';

export const authApi = {
  login: (email, motDePasse, tenantId) =>
    api.post('/auth/login', { email, motDePasse, tenantId }, { timeout: 30000 }),

  refresh: (refreshToken) =>
    api.post('/auth/refresh', { refreshToken }),

  changePassword: (ancienMotDePasse, nouveauMotDePasse) =>
    api.post('/auth/change-password', { ancienMotDePasse, nouveauMotDePasse }),

  getTenants: () =>
    api.get('/auth/tenants', { timeout: 25000 }),
};
