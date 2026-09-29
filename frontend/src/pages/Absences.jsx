import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { presencesApi, playersApi, slotsApi } from '../api';

const DAY_MAP = {
  0: 'DIMANCHE', 1: 'LUNDI', 2: 'MARDI', 3: 'MERCREDI',
  4: 'JEUDI', 5: 'VENDREDI', 6: 'SAMEDI',
};

function weekdayOf(dateStr) {
  if (!dateStr) return '';
  return DAY_MAP[new Date(`${dateStr}T12:00:00`).getDay()];
}

export default function Absences() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ joueurId: '', creneauId: '', dateSeance: '', motif: '' });
  const [formError, setFormError] = useState('');

  const { data: children } = useQuery({
    queryKey: ['my-children-list'],
    queryFn: () => playersApi.getAll({ size: 100 }).then(r => r.data?.content || r.data || []),
  });

  const { data: allSlots } = useQuery({
    queryKey: ['slots-all'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const { data: declared, isLoading } = useQuery({
    queryKey: ['my-absences'],
    queryFn: () => presencesApi.getMyAbsences().then(r => r.data || []),
  });

  const kids = Array.isArray(children) ? children : [];
  const slots = Array.isArray(allSlots) ? allSlots : allSlots?.content || [];
  const myAbsences = Array.isArray(declared) ? declared : [];

  const selectedChild = kids.find(k => k.id === Number(form.joueurId));
  const childSlots = selectedChild
    ? slots.filter(s => !s.categorieId || s.categorieId === selectedChild.categorieId)
    : [];
  const selectedSlot = childSlots.find(s => s.id === Number(form.creneauId));
  const dayMismatch = form.dateSeance && selectedSlot && weekdayOf(form.dateSeance) !== selectedSlot.jourSemaine;

  const declareMutation = useMutation({
    mutationFn: (data) => presencesApi.declare(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-absences'] });
      setForm({ joueurId: '', creneauId: '', dateSeance: '', motif: '' });
      setFormError('');
    },
    onError: (err) => {
      setFormError(err.response?.data?.message || 'Déclaration impossible');
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.joueurId || !form.creneauId || !form.dateSeance) {
      setFormError('Enfant, créneau et date sont obligatoires');
      return;
    }
    if (dayMismatch) {
      setFormError(`Ce créneau a lieu le ${selectedSlot.jourSemaine}, pas le jour choisi`);
      return;
    }
    declareMutation.mutate(form);
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h2 className="font-bebas text-2xl" style={{ color: 'var(--pitch-dark)' }}>Absences</h2>
        <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
          Prévenez l'entraîneur à l'avance — il confirmera le jour de la séance.
        </p>
      </div>

      <div className="panel p-5 mb-6">
        <h3 className="font-bebas text-lg mb-4" style={{ color: 'var(--pitch-dark)' }}>Signaler une absence</h3>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Enfant</label>
              <select
                value={form.joueurId}
                onChange={e => setForm({ ...form, joueurId: e.target.value, creneauId: '' })}
                className="input-field"
                required
              >
                <option value="">Sélectionner…</option>
                {kids.map(k => <option key={k.id} value={k.id}>{k.prenom} {k.nom}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Créneau</label>
              <select
                value={form.creneauId}
                onChange={e => setForm({ ...form, creneauId: e.target.value })}
                className="input-field"
                required
                disabled={!form.joueurId}
              >
                <option value="">Sélectionner…</option>
                {childSlots.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.jourSemaine} {s.heureDebut}-{s.heureFin} · {s.terrain || ''}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Date de la séance</label>
              <input
                type="date"
                value={form.dateSeance}
                min={today}
                onChange={e => setForm({ ...form, dateSeance: e.target.value })}
                className="input-field"
                required
              />
            </div>
            <div>
              <label className="label">Motif (optionnel)</label>
              <input
                type="text"
                value={form.motif}
                onChange={e => setForm({ ...form, motif: e.target.value })}
                className="input-field"
                placeholder="Ex : rendez-vous médical"
                maxLength={200}
              />
            </div>
          </div>
          {formError && (
            <div className="text-sm px-3 py-2 rounded-lg" style={{ background: '#FBE7E7', color: 'var(--red)' }}>
              {formError}
            </div>
          )}
          <button type="submit" disabled={declareMutation.isPending} className="btn-primary text-sm self-start">
            {declareMutation.isPending ? 'Envoi…' : 'Signaler l\'absence'}
          </button>
        </form>
      </div>

      <div className="panel p-5">
        <h3 className="font-bebas text-lg mb-4" style={{ color: 'var(--pitch-dark)' }}>Absences signalées à venir</h3>
        {isLoading ? (
          <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>Chargement…</p>
        ) : myAbsences.length === 0 ? (
          <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>Aucune absence signalée.</p>
        ) : (
          <div className="flex flex-col gap-2.5">
            {myAbsences.map(a => (
              <div key={a.id} className="flex items-center justify-between gap-3 px-4 py-3 rounded-lg border" style={{ borderColor: 'var(--line)' }}>
                <div className="min-w-0">
                  <div className="text-sm font-semibold">{a.joueurPrenom} {a.joueurNom}</div>
                  <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>
                    {a.dateSeance}{a.motif ? ` — ${a.motif}` : ''}
                  </div>
                </div>
                <span className={`pill text-[11px] ${a.present ? 'pill-ok' : 'pill-attente'}`}>
                  {a.present ? 'Confirmée' : 'En attente'}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
