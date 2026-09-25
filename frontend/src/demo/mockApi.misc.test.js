import { describe, it, expect } from 'vitest';
import { handleMockRequest } from './mockApi.js';

describe('mockApi misc endpoints', () => {
  it('lists tenants for the login academy selector', async () => {
    const { data } = await handleMockRequest('GET', '/auth/tenants');

    expect(Array.isArray(data)).toBe(true);
    expect(data.length).toBeGreaterThan(0);
    expect(data[0]).toMatchObject({ id: expect.any(Number) });
    expect(data[0].slug).toBeTruthy();
    expect(data[0].nom).toBeTruthy();
  });

  it('returns dashboard stats with player counts', async () => {
    const { data } = await handleMockRequest('GET', '/dashboard/stats', null, {});

    expect(data.totalPlayers).toBeGreaterThan(0);
    expect(data).toHaveProperty('pendingPayments');
  });

  it('returns paginated audit logs newest first', async () => {
    const { data } = await handleMockRequest('GET', '/audit', null, { page: 0, size: 5 });

    expect(Array.isArray(data.content)).toBe(true);
    expect(data.content.length).toBeLessThanOrEqual(5);
  });

  it('lists categories', async () => {
    const { data } = await handleMockRequest('GET', '/categories', null, {});

    expect(data.totalElements).toBeGreaterThan(0);
    expect(data.content[0].nom).toBeTruthy();
  });

  it('exports payments as Excel XML', async () => {
    const { data } = await handleMockRequest('GET', '/reports/payments/excel');

    expect(typeof data).toBe('string');
    expect(data).toContain('<Workbook');
  });

  it('rejects unknown routes with 404', async () => {
    await expect(handleMockRequest('GET', '/nope/not-here')).rejects.toMatchObject({
      status: 404,
    });
  });
});
