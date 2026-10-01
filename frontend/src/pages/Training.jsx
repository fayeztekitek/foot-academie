import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { slotsApi } from '../api';
import { useAuth } from '../hooks/useAuth';
import CalendarSubscription from '../components/CalendarSubscription';
import { Search, X, CalendarDays, LayoutGrid } from 'lucide-react';

const MONTHS_FR = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];

function monthRange(year, month) {
  const first = `${year}-${String(month + 1).padStart(2, '0')}-01`;
  const lastDay = new Date(year, month + 1, 0).getDate();
  const last = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
  return { first, last };
}

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
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const canEdit = user?.role === 'ADMIN' || user?.role === 'COACH' || user?.role === 'SUPER_ADMIN';
  const [search, setSearch] = useState('');
  const [terrainFilter, setTerrainFilter] = useState('');
  const [view, setView] = useState('week');
  const today = new Date();
  const [calYear, setCalYear] = useState(today.getFullYear());
  const [calMonth, setCalMonth] = useState(today.getMonth());
  const [excOccurrence, setExcOccurrence] = useState(null);
  const [excForm, setExcForm] = useState({ action: 'ANNULEE', heureDebut: '', heureFin: '', terrain: '', motif: '' });
  const { data, isLoading } = useQuery({
    queryKey: ['slots'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const { first: monthFirst, last: monthLast } = monthRange(calYear, calMonth);
  const { data: occurrencesData } = useQuery({
    queryKey: ['slot-occurrences', monthFirst, monthLast],
    queryFn: () => slotsApi.getOccurrences(monthFirst, monthLast).then(r => r.data || []),
    enabled: view === 'month',
  });
  const occurrences = Array.isArray(occurrencesData) ? occurrencesData : [];

  const exceptionMutation = useMutation({
    mutationFn: ({ creneauId, payload }) => slotsApi.saveException(creneauId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['slot-occurrences'] });
      setExcOccurrence(null);
    },
  });

  const restoreMutation = useMutation({
    mutationFn: ({ creneauId, date }) => slotsApi.deleteException(creneauId, date),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['slot-occurrences'] });
      setExcOccurrence(null);
    },
  });

  const openExceptionDialog = (occ) => {
    setExcForm({
      action: 'ANNULEE',
      heureDebut: occ.heureDebut?.slice(0, 5) || '',
      heureFin: occ.heureFin?.slice(0, 5) || '',
      terrain: occ.terrain || '',
      motif: occ.motif || '',
    });
    setExcOccurrence(occ);
  };

  const handleSaveException = () => {
    if (!excOccurrence || !excForm.motif.trim()) return;
    exceptionMutation.mutate({
      creneauId: excOccurrence.creneauId,
      payload: {
        date: excOccurrence.date,
        statut: excForm.action,
        heureDebut: excForm.action === 'MODIFIEE' ? (excForm.heureDebut || null) : null,
        heureFin: excForm.action === 'MODIFIEE' ? (excForm.heureFin || null) : null,
        terrain: excForm.action === 'MODIFIEE' ? (excForm.terrain || null) : null,
        motif: excForm.motif.trim(),
      },
    });
  };

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

  const byDate = {};
  occurrences.forEach(o => {
    if (!byDate[o.date]) byDate[o.date] = [];
    byDate[o.date].push(o);
  });

  const monthCells = () => {
    const firstDow = (new Date(calYear, calMonth, 1).getDay() + 6) % 7;
    const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate();
    const cells = [];
    for (let i = 0; i < firstDow; i++) cells.push(null);
    for (let d = 1; d <= daysInMonth; d++) {
      cells.push(`${calYear}-${String(calMonth + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`);
    }
    return cells;
  };

  return (
    <div>
      <div className="flex items-center justify-between gap-3 mb-5 flex-wrap">
        <h2 className="font-bebas text-2xl m-0" style={{ color: 'var(--pitch-dark)' }}>Calendrier des entraînements</h2>
        <div className="flex rounded-lg border overflow-hidden" style={{ borderColor: 'var(--line)' }}>
          <button
            type="button"
            onClick={() => setView('week')}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold"
            style={{
              background: view === 'week' ? 'var(--pitch-dark)' : '#fff',
              color: view === 'week' ? '#fff' : 'var(--ink-soft)',
            }}
          >
            <LayoutGrid size={13} /> Semaine type
          </button>
          <button
            type="button"
            onClick={() => setView('month')}
            className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold"
            style={{
              background: view === 'month' ? 'var(--pitch-dark)' : '#fff',
              color: view === 'month' ? '#fff' : 'var(--ink-soft)',
            }}
          >
            <CalendarDays size={13} /> Calendrier
          </button>
        </div>
      </div>

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

      {view === 'month' && (
        <>
          <div className="flex items-center gap-2 mb-3">
            <button type="button" onClick={() => { if (calMonth === 0) { setCalMonth(11); setCalYear(calYear - 1); } else setCalMonth(calMonth - 1); }} className="btn-ghost btn-sm">‹</button>
            <span className="font-bebas text-lg" style={{ color: 'var(--pitch-dark)' }}>{MONTHS_FR[calMonth]} {calYear}</span>
            <button type="button" onClick={() => { if (calMonth === 11) { setCalMonth(0); setCalYear(calYear + 1); } else setCalMonth(calMonth + 1); }} className="btn-ghost btn-sm">›</button>
          </div>
          <div className="overflow-x-auto">
            <div className="border min-w-[680px]" style={{ borderColor: 'var(--line)', background: 'var(--line)', display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '1px' }}>
              {DAYS_SHORT.map(d => (
                <div key={d} className="py-2 px-1.5 text-center text-[11.5px] font-semibold" style={{ background: '#F9F8F3', color: 'var(--ink-soft)' }}>{d}</div>
              ))}
              {monthCells().map((dateStr, i) => (
                <div key={i} className="bg-white min-h-[86px] p-1">
                  {dateStr && (
                    <>
                      <div className="text-[11px] font-semibold mb-1" style={{ color: 'var(--ink-soft)' }}>{Number(dateStr.slice(8))}</div>
                      {(byDate[dateStr] || []).map(o => {
                        const colors = getCatColor(o.categorieNom);
                        const cancelled = o.statut === 'ANNULEE';
                        return (
                          <button
                            key={`${o.creneauId}-${o.date}`}
                            type="button"
                            disabled={!canEdit}
                            onClick={() => canEdit && openExceptionDialog(o)}
                            title={o.motif ? `${o.motif}` : `${o.categorieNom || ''} · ${o.terrain || ''}`}
                            className="block w-full text-left rounded px-1.5 py-1 mb-1 text-[10.5px] font-semibold leading-tight overflow-hidden"
                            style={{
                              background: cancelled ? '#F3F4F6' : colors.bg,
                              color: cancelled ? 'var(--ink-muted)' : colors.color,
                              borderLeft: `3px solid ${cancelled ? 'var(--line)' : colors.border}`,
                              textDecoration: cancelled ? 'line-through' : 'none',
                              cursor: canEdit ? 'pointer' : 'default',
                            }}
                          >
                            {o.heureDebut?.slice(0, 5)} {o.categorieNom}
                            {o.statut === 'MODIFIEE' && ' ✎'}
                          </button>
                        );
                      })}
                    </>
                  )}
                </div>
              ))}
            </div>
          </div>
          <p className="text-[11px] mt-2" style={{ color: 'var(--ink-soft)' }}>
            {canEdit
              ? 'Cliquez sur une séance pour l\'annuler ou la modifier avec un motif.'
              : 'Séances affichées par date exacte, modifications du planning incluses.'}
          </p>
        </>
      )}

      {view === 'week' && (
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
                      const colors = getCatColor(slot.categorieNom);
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
      )}

      {excOccurrence && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[420px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>
                Séance du {excOccurrence.date}
              </h3>
              <p className="text-xs mt-1" style={{ color: 'var(--ink-soft)' }}>
                {excOccurrence.categorieNom} · {excOccurrence.heureDebut?.slice(0, 5)}-{excOccurrence.heureFin?.slice(0, 5)} · {excOccurrence.terrain || '—'}
                {excOccurrence.statut !== 'NORMALE' && ` · ${excOccurrence.statut} (${excOccurrence.motif || ''})`}
              </p>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div>
                <label className="label">Action</label>
                <select
                  value={excForm.action}
                  onChange={e => setExcForm({ ...excForm, action: e.target.value })}
                  className="input-field"
                >
                  <option value="ANNULEE">Annuler la séance</option>
                  <option value="MODIFIEE">Modifier (horaire / lieu)</option>
                </select>
              </div>
              {excForm.action === 'MODIFIEE' && (
                <>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="label">Début</label>
                      <input type="time" value={excForm.heureDebut} onChange={e => setExcForm({ ...excForm, heureDebut: e.target.value })} className="input-field" />
                    </div>
                    <div>
                      <label className="label">Fin</label>
                      <input type="time" value={excForm.heureFin} onChange={e => setExcForm({ ...excForm, heureFin: e.target.value })} className="input-field" />
                    </div>
                  </div>
                  <div>
                    <label className="label">Terrain</label>
                    <input type="text" value={excForm.terrain} onChange={e => setExcForm({ ...excForm, terrain: e.target.value })} className="input-field" />
                  </div>
                </>
              )}
              <div>
                <label className="label">Motif *</label>
                <input
                  type="text"
                  value={excForm.motif}
                  onChange={e => setExcForm({ ...excForm, motif: e.target.value })}
                  className="input-field"
                  placeholder="Ex : terrain impraticable, tournoi…"
                  required
                />
              </div>
            </div>
            <div className="flex justify-between gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <div>
                {excOccurrence.statut !== 'NORMALE' && (
                  <button
                    type="button"
                    onClick={() => restoreMutation.mutate({ creneauId: excOccurrence.creneauId, date: excOccurrence.date })}
                    disabled={restoreMutation.isPending}
                    className="btn-ghost text-sm"
                  >
                    Restaurer
                  </button>
                )}
              </div>
              <div className="flex gap-2.5">
                <button type="button" onClick={() => setExcOccurrence(null)} className="btn-ghost text-sm">Annuler</button>
                <button
                  type="button"
                  onClick={handleSaveException}
                  disabled={!excForm.motif.trim() || exceptionMutation.isPending}
                  className="btn-primary text-sm"
                >
                  {exceptionMutation.isPending ? 'Enregistrement…' : 'Enregistrer'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="mt-6 max-w-2xl">
        <CalendarSubscription />
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
