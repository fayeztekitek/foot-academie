import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { slotsApi } from '../api';
import { Search, X } from 'lucide-react';

const DAYS = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
const DAYS_SHORT = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
const HOURS = [];
for (let h = 8; h <= 23; h++) HOURS.push(`${String(h).padStart(2, '0')}:00`);

const CAT_COLORS = {
  U9: { bg: '#DCEEE2', color: '#1F5A3B', border: 'var(--grass)' },
  U11: { bg: '#DCEEE2', color: '#1F5A3B', border: 'var(--grass)' },
  U13: { bg: '#E9E1C9', color: '#7A5C10', border: 'var(--gold)' },
  U15: { bg: '#F6DADE', color: '#8E1B2E', border: 'var(--red)' },
  U17: { bg: '#F6DADE', color: '#8E1B2E', border: 'var(--red)' },
  Élite: { bg: '#D8E3EC', color: '#1E3E57', border: '#3A6C95' },
};

function getCatColor(catName) {
  if (!catName) return { bg: '#E6F1EA', color: '#1F5A3B', border: 'var(--grass)' };
  const key = Object.keys(CAT_COLORS).find(k => catName.toLowerCase().includes(k.toLowerCase()));
  return key ? CAT_COLORS[key] : { bg: '#E6F1EA', color: '#1F5A3B', border: 'var(--grass)' };
}

function getDayIndex(jour) {
  const map = { LUNDI: 0, MARDI: 1, MERCREDI: 2, JEUDI: 3, VENDREDI: 4, SAMEDI: 5, DIMANCHE: 6 };
  return map[jour?.toUpperCase()] ?? -1;
}

function getHourIndex(heureDebut) {
  if (!heureDebut) return -1;
  const h = parseInt(heureDebut.split(':')[0], 10);
  return h - 8;
}

export default function Training() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [terrainFilter, setTerrainFilter] = useState('');
  const { data, isLoading } = useQuery({
    queryKey: ['slots'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const slots = Array.isArray(data) ? data : data?.content || [];

  const filteredSlots = slots.filter(s => {
    if (search) {
      const q = search.toLowerCase();
      const matchCat = (s.categorieNom || '').toLowerCase().includes(q);
      const matchCoach = `${s.entraineurPrenom || ''} ${s.entraineurNom || ''}`.toLowerCase().includes(q);
      if (!matchCat && !matchCoach) return false;
    }
    if (terrainFilter && s.terrain !== terrainFilter) return false;
    return true;
  });

  const terrains = [...new Set(slots.map(s => s.terrain).filter(Boolean))];

  const grid = {};
  filteredSlots.forEach(slot => {
    const dayIdx = getDayIndex(slot.jourSemaine);
    const hourIdx = getHourIndex(slot.heureDebut);
    if (dayIdx >= 0 && hourIdx >= 0) {
      const key = `${hourIdx}-${dayIdx}`;
      if (!grid[key]) grid[key] = [];
      grid[key].push(slot);
    }
  });

  if (isLoading) return (
    <div>
      <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>Calendrier des entraînements</h2>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}
      </div>
    </div>
  );

  return (
    <div>
      <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>Calendrier des entraînements</h2>

      {/* Filters */}
      <div className="flex gap-3 mb-4 flex-wrap items-center">
        <div className="relative flex-1 min-w-[200px] max-w-[300px]">
          <Search size={15} className="absolute left-3 top-2.5" style={{ color: 'var(--ink-soft)' }} />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Rechercher catégorie ou entraîneur…"
            className="input-field pl-9 text-xs"
          />
        </div>
        {terrains.length > 0 && (
          <select
            value={terrainFilter}
            onChange={e => setTerrainFilter(e.target.value)}
            className="input-field text-xs py-1.5 px-3"
          >
            <option value="">Tous terrains</option>
            {terrains.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        )}
        {(search || terrainFilter) && (
          <button
            onClick={() => { setSearch(''); setTerrainFilter(''); }}
            className="text-xs flex items-center gap-1 px-2 py-1 rounded border cursor-pointer hover:bg-gray-50"
            style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}
          >
            <X size={12} /> Effacer
          </button>
        )}
      </div>

      <div className="overflow-x-auto">
        <div className="border min-w-[680px]" style={{ borderColor: 'var(--line)', background: 'var(--line)', display: 'grid', gridTemplateColumns: `70px repeat(7, 1fr)`, gap: '1px' }}>
          {/* Header row */}
          <div className="py-2.5 px-1.5 text-center text-[11.5px] font-semibold" style={{ background: '#F9F8F3', color: 'var(--ink-soft)' }}></div>
          {DAYS_SHORT.map(d => (
            <div key={d} className="py-2.5 px-1.5 text-center text-[11.5px] font-semibold" style={{ background: '#F9F8F3', color: 'var(--ink-soft)' }}>{d}</div>
          ))}

          {/* Hour rows */}
          {HOURS.map((hour, hi) => (
            <div key={hour} className="contents">
              <div className="py-2 px-1.5 text-right text-[10.5px]" style={{ background: '#F9F8F3', color: 'var(--ink-soft)' }}>{hour}</div>
              {DAYS.map((_, di) => {
                const cellSlots = grid[`${hi}-${di}`] || [];
                return (
                  <div key={di} className="bg-white min-h-[52px] relative p-[3px]">
                    {cellSlots.map((slot, si) => {
                      const colors = getCatColor(slot.categorie?.nom);
                      return (
                        <div
                          key={si}
                          className="absolute inset-[2px] rounded px-1.5 py-1 text-[10.5px] font-semibold leading-tight overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
                          style={{ background: colors.bg, color: colors.color, borderLeft: `3px solid ${colors.border}` }}
                          title={`${slot.categorieNom} — ${slot.entraineurs?.length > 0 ? slot.entraineurs.map(e => `${e.prenom} ${e.nom}`).join(', ') : `${slot.entraineurPrenom} ${slot.entraineurNom}`}`}
                          onClick={() => navigate(`/presence?slot=${slot.id}`)}
                        >
                          {slot.categorieNom} · {slot.terrain || '—'}
                          {slot.entraineurs && slot.entraineurs.length > 1 && (
                            <span className="ml-1 opacity-70">({slot.entraineurs.length})</span>
                          )}
                        </div>
                      );
                    })}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      {/* Legend */}
      <div className="flex gap-4 mt-4 flex-wrap">
        <span className="flex items-center gap-1.5 text-xs" style={{ color: 'var(--ink-soft)' }}>
          <span className="w-2.5 h-2.5 rounded-sm inline-block" style={{ background: 'var(--grass)' }}></span>
          U9 — Formation
        </span>
        <span className="flex items-center gap-1.5 text-xs" style={{ color: 'var(--ink-soft)' }}>
          <span className="w-2.5 h-2.5 rounded-sm inline-block" style={{ background: 'var(--gold)' }}></span>
          U13 — Élite
        </span>
        <span className="flex items-center gap-1.5 text-xs" style={{ color: 'var(--ink-soft)' }}>
          <span className="w-2.5 h-2.5 rounded-sm inline-block" style={{ background: 'var(--red)' }}></span>
          U17 — Pré-nationale
        </span>
        <span className="flex items-center gap-1.5 text-xs" style={{ color: 'var(--ink-soft)' }}>
          <span className="w-2.5 h-2.5 rounded-sm inline-block" style={{ background: '#3A6C95' }}></span>
          Groupe Élite
        </span>
      </div>
    </div>
  );
}
