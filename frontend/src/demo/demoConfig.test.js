import { describe, it, expect, beforeEach } from 'vitest';
import {
  isDemoMode,
  setDemoMode,
  getApiBaseUrl,
  setApiBaseUrl,
  getBackendUrl,
  setBackendUrl,
} from './demoConfig.js';

function stubLocalStorage() {
  const store = new Map();
  globalThis.localStorage = {
    getItem: (k) => (store.has(k) ? store.get(k) : null),
    setItem: (k, v) => store.set(k, String(v)),
    removeItem: (k) => store.delete(k),
    clear: () => store.clear(),
  };
  return store;
}

describe('demoConfig', () => {
  beforeEach(() => {
    stubLocalStorage();
  });

  it('is not in demo mode by default', () => {
    expect(isDemoMode()).toBe(false);
  });

  it('toggles demo mode on and off', () => {
    setDemoMode(true);
    expect(isDemoMode()).toBe(true);
    setDemoMode(false);
    expect(isDemoMode()).toBe(false);
  });

  it('disabling demo mode clears custom backend URLs', () => {
    setApiBaseUrl('https://custom.ex/api');
    setDemoMode(true);
    setDemoMode(false);
    expect(localStorage.getItem('nadi_api_base_url')).toBeNull();
    expect(localStorage.getItem('nadi_backend_url')).toBeNull();
  });

  it('falls back to the baked-in API base URL when nothing is stored', () => {
    // __VITE_API_URL__ is only defined in vite builds; in vitest it is
    // undefined, so the default '/api' must be returned without throwing.
    expect(getApiBaseUrl()).toBe('/api');
  });

  it('prefers a stored API base URL over the default', () => {
    setApiBaseUrl('https://custom.ex/api');
    expect(getApiBaseUrl()).toBe('https://custom.ex/api');
  });

  it('ignores junk stored values (undefined/null/empty)', () => {
    for (const junk of ['undefined', 'null', '']) {
      localStorage.setItem('nadi_api_base_url', junk);
      expect(getApiBaseUrl()).toBe('/api');
    }
  });

  it('derives the backend URL by stripping a trailing /api', () => {
    setBackendUrl('https://custom.ex');
    expect(getBackendUrl()).toBe('https://custom.ex');
    localStorage.setItem('nadi_backend_url', 'undefined');
    expect(getBackendUrl()).toBe('');
  });
});
