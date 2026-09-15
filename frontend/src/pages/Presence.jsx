import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { slotsApi, playersApi, presencesApi } from '../api';
import { useAuth } from '../hooks/useAuth';
import { Check, X, Save } from 'lucide-react';

const DAY_MAP = {
  0: 'DIMANCHE', 1: 'LUNDI', 2: 'MARDI', 3: 'MERCREDI',
  4: 'JEUDI', 5: 'VENDREDI', 6: 'SAMEDI',
};

function getTodayStr() {
  return new Date().toISOString().split('T')[0];
}

function getTodayDayName() {
  return DAY_MAP[new Date().getDay()];
}

export default function Presence() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [searchParams] = useSearchParams();
  const [selectedSlotId, setSelectedSlotId] = useState(searchParams.get('slot') || '');
  const [selectedDate, setSelectedDate] = useState(getTodayStr());
  const [marks, setMarks] = useState({});

  useEffect(() => {
    const slotParam = searchParams.get('slot');
    if (slotParam) setSelectedSlotId(slotParam);
  }, [searchParams]);

  const { data: allSlots } = useQuery({
    queryKey: ['slots-all'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const slots = Array.isArray(allSlots) ? allSlots : allSlots?.content || [];
  const selectedSlot = slots.find(s => s.id === Number(selectedSlotId));

  const todayDayName = getTodayDayName();
  const todaySlots = slots.filter(s => s.jourSemaine === todayDayName);

  const { data: players, isLoading: playersLoading } = useQuery({
    queryKey: ['players-by-cat', selectedSlot?.categorieId],
    queryFn: () => playersApi.getAll({ categorieId: selectedSlot.categorieId, size: 100 }).then(r => r.data?.content || r.data),
    enabled: !!selectedSlot?.categorieId,
  });

  const { data: existingPresences } = useQuery({
    queryKey: ['presences', selectedSlotId, selectedDate],
    queryFn: () => presencesApi.getByCreneauAndDate(selectedSlotId, selectedDate).then(r => r.data),
    enabled: !!selectedSlotId && !!selectedDate,
  });

  const saveMutation = useMutation({
    mutationFn: (data) => presencesApi.save(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['presences', selectedSlotId, selectedDate]);
    },
  });

  const playerList = Array.isArray(players) ? players : [];

  const initMarks = () => {
    if (!existingPresences || !playerList.length) return;
    const m = {};
    playerList.forEach(p => {
      const existing = existingPresences.find(e => e.joueurId === p.id);
      m[p.id] = existing ? existing.present : false;
    });
    setMarks(m);
  };

  if (existingPresences && Object.keys(marks).length === 0 && playerList.length > 0) {
    initMarks();
  }

  const toggle = (joueurId) => {
    setMarks(prev => ({ ...prev, [joueurId]: !prev[joueurId] }));
  };

  const handleSave = () => {
    if (!selectedSlotId || !selectedDate) return;
    const presences = playerList.map(p => ({
      joueurId: p.id,
      present: !!marks[p.id],
    }));
    saveMutation.mutate({
      creneauId: Number(selectedSlotId),
      dateSeance: selectedDate,
      presences,
    });
  };

  const presentCount = Object.values(marks).filter(Boolean).length;
  const totalPlayers = playerList.length;

  return (
    <div>
      <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>Marquer la présence</h2>

      {/* Selectors */}
      <div className="panel p-5 mb-5">
        <div className="flex gap-4 flex-wrap items-end">
          <div className="flex-1 min-w-[200px]">
            <label className="label">Date de séance</label>
            <input type="date" value={selectedDate} onChange={e => { setSelectedDate(e.target.value); setMarks({}); }} className="input-field" />
          </div>
          <div className="flex-1 min-w-[260px]">
            <label className="label">Créneau d'entraînement</label>
            <select value={selectedSlotId} onChange={e => { setSelectedSlotId(e.target.value); setMarks({}); }} className="input-field">
              <option value="">Choisir un créneau...</option>
              {todaySlots.length > 0 && (
                <optgroup label="Aujourd'hui">
                  {todaySlots.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.heureDebut?.slice(0, 5)}–{s.heureFin?.slice(0, 5)} · {s.categorieNom} · {s.terrain || '—'}
                    </option>
                  ))}
                </optgroup>
              )}
              <optgroup label="Tous les créneaux">
                {slots.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.jourSemaine?.charAt(0) + s.jourSemaine?.slice(1).toLowerCase()} {s.heureDebut?.slice(0, 5)}–{s.heureFin?.slice(0, 5)} · {s.categorieNom} · {s.terrain || '—'}
                  </option>
                ))}
              </optgroup>
            </select>
          </div>
          {selectedSlotId && playerList.length > 0 && (
            <button onClick={handleSave} disabled={saveMutation.isPending} className="btn-primary flex items-center gap-1.5 text-sm whitespace-nowrap">
              <Save size={15} />
              {saveMutation.isPending ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          )}
        </div>
        {selectedSlot && (
          <div className="mt-3 flex gap-4 text-xs" style={{ color: 'var(--ink-soft)' }}>
            <span><strong>{selectedSlot.categorieNom}</strong> — {selectedSlot.terrain || '—'}</span>
            <span>Coach {selectedSlot.entraineurPrenom} {selectedSlot.entraineurNom}</span>
            {totalPlayers > 0 && (
              <span className={presentCount === totalPlayers ? 'text-grass font-semibold' : ''}>
                {presentCount}/{totalPlayers} présents
              </span>
            )}
          </div>
        )}
      </div>

      {/* Player list */}
      {!selectedSlotId ? (
        <div className="panel p-8 text-center" style={{ color: 'var(--ink-soft)' }}>
          Sélectionnez un créneau pour marquer les présences.
        </div>
      ) : playersLoading ? (
        <div className="panel p-8 text-center" style={{ color: 'var(--ink-soft)' }}>Chargement des joueurs...</div>
      ) : playerList.length === 0 ? (
        <div className="panel p-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun joueur dans cette catégorie.</div>
      ) : (
        <div className="panel">
          <div className="overflow-x-auto">
            <table className="w-full text-[13.5px]">
              <thead>
                <tr>
                  {['Joueur', 'Né en', 'Présent', ''].map(h => (
                    <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-5 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {playerList.map(p => (
                  <tr key={p.id} className="border-b last:border-b-0" style={{ borderColor: '#F0EEE4' }}>
                    <td className="py-3 px-5">
                      <div className="font-semibold">{p.prenom} {p.nom}</div>
                    </td>
                    <td className="py-3 px-5" style={{ color: 'var(--ink-soft)' }}>
                      {p.dateNaissance ? p.dateNaissance.split('-')[0] : '—'}
                    </td>
                    <td className="py-3 px-5">
                      <button
                        onClick={() => toggle(p.id)}
                        className={`w-9 h-9 rounded-lg flex items-center justify-center border-2 transition-all cursor-pointer ${
                          marks[p.id]
                            ? 'bg-grass border-grass text-white'
                            : 'bg-white border-gray-300 text-gray-300 hover:border-gray-400'
                        }`}
                      >
                        {marks[p.id] ? <Check size={16} /> : <X size={16} />}
                      </button>
                    </td>
                    <td className="py-3 px-5 text-xs" style={{ color: 'var(--ink-soft)' }}>
                      {marks[p.id] ? 'Présent' : 'Absent'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
