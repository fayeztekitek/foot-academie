import axios from 'axios';
import { isDemoMode, getApiBaseUrl } from '../demo/demoConfig';

const API_BASE = getApiBaseUrl();

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

function clearSessionAndRedirect() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login';
}

// Single-flight refresh: concurrent 401s share one refresh call instead of
// firing N parallel refresh POSTs that can invalidate each other.
let refreshPromise = null;
function doRefresh() {
  if (!refreshPromise) {
    const refreshToken = localStorage.getItem('refreshToken');
    refreshPromise = axios.post(`${API_BASE}/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        localStorage.setItem('accessToken', data.accessToken);
        if (data.refreshToken) {
          localStorage.setItem('refreshToken', data.refreshToken);
        }
        return data.accessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

api.interceptors.request.use(async (config) => {
  if (isDemoMode()) {
    // Lazy import keeps the demo mock dataset out of the production bundle.
    const { handleMockRequest } = await import('../demo/mockApi');
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
    const status = error.response?.status;

    // Backend-enforced password rotation: stop, flag the session, and let the
    // router show the forced-change screen instead of failing silently.
    if (status === 403 && error.response?.data?.code === 'PASSWORD_CHANGE_REQUIRED') {
      localStorage.setItem('mustChangePassword', 'true');
      window.location.href = '/';
      return Promise.reject(error);
    }

    // Only an expired access token (401) justifies spending the refresh
    // token. A 403 means "authenticated but forbidden" — retrying with a new
    // token cannot help and only burns the refresh token.
    if (status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }
    originalRequest._retry = true;

    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      clearSessionAndRedirect();
      return Promise.reject(error);
    }
    try {
      const newAccessToken = await doRefresh();
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return api(originalRequest);
    } catch {
      clearSessionAndRedirect();
      return Promise.reject(error);
    }
  }
);

export default api;
