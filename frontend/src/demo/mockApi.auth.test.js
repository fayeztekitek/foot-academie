import { describe, it, expect } from 'vitest';
import { handleMockRequest } from './mockApi.js';

describe('mockApi auth', () => {
  it('logs in the admin with tokens and role', async () => {
    const { data } = await handleMockRequest('POST', '/auth/login', {
      email: 'admin@nadi.tn',
      motDePasse: 'admin123',
    });

    expect(data.role).toBe('ADMIN');
    expect(data.accessToken).toBeTruthy();
    expect(data.refreshToken).toBeTruthy();
    expect(data.email).toBe('admin@nadi.tn');
  });

  it('rejects a wrong password with 401', async () => {
    await expect(
      handleMockRequest('POST', '/auth/login', {
        email: 'admin@nadi.tn',
        motDePasse: 'not-the-password',
      })
    ).rejects.toMatchObject({ status: 401 });
  });

  it('rejects an unknown email with 401', async () => {
    await expect(
      handleMockRequest('POST', '/auth/login', {
        email: 'ghost@nadi.tn',
        motDePasse: 'whatever12',
      })
    ).rejects.toMatchObject({ status: 401 });
  });

  it('logs in the coach with the COACH role', async () => {
    const { data } = await handleMockRequest('POST', '/auth/login', {
      email: 'coach@nadi.tn',
      motDePasse: 'coach123',
    });

    expect(data.role).toBe('COACH');
  });

  it('logs in the parent with the PARENT role', async () => {
    const { data } = await handleMockRequest('POST', '/auth/login', {
      email: 'parent@nadi.tn',
      motDePasse: 'parent123',
    });

    expect(data.role).toBe('PARENT');
  });
});
