const DEMO_MODE_KEY = 'nadi_demo_mode';
const API_BASE_URL_KEY = 'nadi_api_base_url';

// VITE_API_URL is set at build time via vite.config.js
const VITE_API_URL = typeof __VITE_API_URL__ !== 'undefined' ? __VITE_API_URL__ : '';

export function isDemoMode() {
  try {
    return localStorage.getItem(DEMO_MODE_KEY) === 'true';
  } catch {
    return false;
  }
}

export function setDemoMode(enabled) {
  try {
    localStorage.setItem(DEMO_MODE_KEY, enabled ? 'true' : 'false');
    if (!enabled) {
      localStorage.removeItem(API_BASE_URL_KEY);
      localStorage.removeItem('nadi_backend_url');
    }
  } catch {}
}

export function getApiBaseUrl() {
  try {
    const stored = localStorage.getItem(API_BASE_URL_KEY);
    if (stored && stored !== '' && stored !== 'undefined' && stored !== 'null') return stored;
    return VITE_API_URL || '/api';
  } catch {
    return VITE_API_URL || '/api';
  }
}

export function setApiBaseUrl(url) {
  try {
    localStorage.setItem(API_BASE_URL_KEY, url || '/api');
  } catch {}
}

export function getBackendUrl() {
  try {
    const stored = localStorage.getItem('nadi_backend_url');
    if (stored && stored !== '' && stored !== 'undefined' && stored !== 'null') return stored;
    return VITE_API_URL ? VITE_API_URL.replace(/\/api$/, '') : '';
  } catch {
    return VITE_API_URL ? VITE_API_URL.replace(/\/api$/, '') : '';
  }
}

export function setBackendUrl(url) {
  try {
    localStorage.setItem('nadi_backend_url', url || '');
  } catch {}
}
