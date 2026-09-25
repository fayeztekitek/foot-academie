import { describe, it, expect } from 'vitest';
import { handleMockRequest } from './mockApi.js';

describe('mockApi players', () => {
  it('lists players in paginated shape', async () => {
    const { data } = await handleMockRequest('GET', '/players', null, { page: 0, size: 5 });

    expect(Array.isArray(data.content)).toBe(true);
    expect(data.totalElements).toBeGreaterThan(0);
    expect(data.content.length).toBeLessThanOrEqual(5);
  });

  it('searches players by name', async () => {
    const { data } = await handleMockRequest('GET', '/players', null, { search: 'Trabelsi' });

    expect(data.totalElements).toBeGreaterThan(0);
    for (const p of data.content) {
      expect(`${p.prenom} ${p.nom}`.toLowerCase()).toContain('trabelsi');
    }
  });

  it('creates a player with a fresh id', async () => {
    const before = await handleMockRequest('GET', '/players', null, {});
    const { data } = await handleMockRequest('POST', '/players', {
      prenom: 'TestVitest',
      nom: 'Unique',
      dateNaissance: '2014-01-01',
    });

    expect(data.id).toBeDefined();
    expect(data.prenom).toBe('TestVitest');
    const after = await handleMockRequest('GET', '/players', null, {});
    expect(after.data.totalElements).toBe(before.data.totalElements + 1);
  });

  it('updates a player and persists the change', async () => {
    const created = await handleMockRequest('POST', '/players', {
      prenom: 'ToUpdate',
      nom: 'Unique',
      dateNaissance: '2014-01-01',
    });

    const { data } = await handleMockRequest('PUT', `/players/${created.data.id}`, {
      prenom: 'UpdatedName',
    });

    expect(data.prenom).toBe('UpdatedName');
    expect(data.nom).toBe('Unique');
  });

  it('deletes a player and returns 404 afterwards', async () => {
    const created = await handleMockRequest('POST', '/players', {
      prenom: 'ToDelete',
      nom: 'Unique',
      dateNaissance: '2014-01-01',
    });

    await handleMockRequest('DELETE', `/players/${created.data.id}`);
    await expect(handleMockRequest('GET', `/players/${created.data.id}`)).rejects.toMatchObject({
      status: 404,
    });
  });

  it('rejects updating an unknown player with 404', async () => {
    await expect(
      handleMockRequest('PUT', '/players/987654', { prenom: 'X' })
    ).rejects.toMatchObject({ status: 404 });
  });
});
