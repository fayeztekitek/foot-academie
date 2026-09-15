import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { slotsApi } from '../api';

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
  const { data, isLoading } = useQuery({
    queryKey: ['slots'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const slots = Array.isArray(data) ? data : data?.content || [];

  const grid = {};
  slots.forEach(slot => {
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
                          title={`${slot.categorieNom} — ${slot.entraineurPrenom} ${slot.entraineurNom}`}
                          onClick={() => navigate(`/presence?slot=${slot.id}`)}
                        >
                          {slot.categorieNom} · {slot.terrain || '—'}
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
