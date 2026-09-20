import axios from 'axios';
import { isDemoMode, getApiBaseUrl } from '../demo/demoConfig';
import { handleMockRequest } from '../demo/mockApi';

const API_BASE = getApiBaseUrl();

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  if (isDemoMode()) {
    const method = (config.method || 'get').toUpperCase();
    const url = config.url || '';
    const params = config.params;
    const data = config.data;

    config.adapter = () => {
      return handleMockRequest(method, url, data || {}, params || {})
        .then(result => ({
          data: result.data,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
        }))
        .catch(err => Promise.reject({
          response: { status: err.status || 500, data: { message: err.message } },
        }));
    };
    return config;
  }

  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (isDemoMode()) {
      return Promise.reject(error);
    }
    const originalRequest = error.config;
    if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken });
          localStorage.setItem('accessToken', data.accessToken);
          originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
          return api(originalRequest);
        } catch {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      } else {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
