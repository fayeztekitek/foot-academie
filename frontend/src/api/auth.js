import api from './client';

export const authApi = {
  login: (email, motDePasse) =>
    api.post('/auth/login', { email, motDePasse }),

  refresh: (refreshToken) =>
    api.post('/auth/refresh', { refreshToken }),

  changePassword: (ancienMotDePasse, nouveauMotDePasse) =>
    api.post('/auth/change-password', { ancienMotDePasse, nouveauMotDePasse }),
};
