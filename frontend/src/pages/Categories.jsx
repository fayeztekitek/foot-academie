import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { categoriesApi, slotsApi, coachesApi } from '../api';
import { Plus, X, Pencil, Trash2 } from 'lucide-react';

const DAYS = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE'];
const TERRAINS = ['Terrain principal', 'Terrain secondaire', 'Terrain A', 'Terrain B', 'Salle'];

export default function Categories() {
  const queryClient = useQueryClient();
  const [showCatModal, setShowCatModal] = useState(false);
  const [editCat, setEditCat] = useState(null);
  const [deleteCatConfirm, setDeleteCatConfirm] = useState(null);
  const [showSlotModal, setShowSlotModal] = useState(false);
  const [editSlot, setEditSlot] = useState(null);
  const [deleteSlotConfirm, setDeleteSlotConfirm] = useState(null);
  const [catForm, setCatForm] = useState({ nom: '', description: '', ageMin: '', ageMax: '' });
  const [slotForm, setSlotForm] = useState({ jourSemaine: 'LUNDI', heureDebut: '', heureFin: '', categorieId: '', entraineurId: '', terrain: '' });

  const { data: categories, isLoading: catLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoriesApi.getAll({ size: 50 }).then(r => r.data?.content || r.data),
  });

  const { data: slots, isLoading: slotLoading } = useQuery({
    queryKey: ['slots-list'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  const { data: coaches } = useQuery({
    queryKey: ['coaches-list'],
    queryFn: () => coachesApi.getList().then(r => r.data),
  });

  const createCatMutation = useMutation({
    mutationFn: (data) => categoriesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['categories']);
      setShowCatModal(false);
      setCatForm({ nom: '', description: '', ageMin: '', ageMax: '' });
    },
  });

  const updateCatMutation = useMutation({
    mutationFn: ({ id, data }) => categoriesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['categories']);
      setEditCat(null);
      setCatForm({ nom: '', description: '', ageMin: '', ageMax: '' });
    },
  });

  const deleteCatMutation = useMutation({
    mutationFn: (id) => categoriesApi.delete(id),
    onSuccess: () => { queryClient.invalidateQueries(['categories']); setDeleteCatConfirm(null); },
  });

  const createSlotMutation = useMutation({
    mutationFn: (data) => slotsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['slots-list']);
      setShowSlotModal(false);
      setSlotForm({ jourSemaine: 'LUNDI', heureDebut: '', heureFin: '', categorieId: '', entraineurId: '', terrain: '' });
    },
  });

  const updateSlotMutation = useMutation({
    mutationFn: ({ id, data }) => slotsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['slots-list']);
      setEditSlot(null);
    },
  });

  const deleteSlotMutation = useMutation({
    mutationFn: (id) => slotsApi.delete(id),
    onSuccess: () => { queryClient.invalidateQueries(['slots-list']); setDeleteSlotConfirm(null); },
  });

  const cats = Array.isArray(categories) ? categories : [];
  const slotsList = Array.isArray(slots) ? slots : slots?.content || [];
  const coachList = Array.isArray(coaches) ? coaches : [];

  const openEditCat = (c) => {
    setCatForm({ nom: c.nom || '', description: c.description || '', ageMin: c.ageMin || '', ageMax: c.ageMax || '' });
    setEditCat(c);
  };

  const openEditSlot = (s) => {
    setSlotForm({
      jourSemaine: s.jourSemaine || 'LUNDI',
      heureDebut: s.heureDebut || '',
      heureFin: s.heureFin || '',
      categorieId: s.categorieId || '',
      entraineurId: s.entraineurId || '',
      terrain: s.terrain || '',
    });
    setEditSlot(s);
  };

  const handleCatSubmit = () => {
    if (editCat) {
      updateCatMutation.mutate({ id: editCat.id, data: catForm });
    } else {
      createCatMutation.mutate(catForm);
    }
  };

  const handleSlotSubmit = () => {
    if (editSlot) {
      updateSlotMutation.mutate({ id: editSlot.id, data: slotForm });
    } else {
      createSlotMutation.mutate(slotForm);
    }
  };

  return (
    <div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 items-start">
        {/* Slots panel */}
        <div className="panel">
          <div className="flex justify-between items-center px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
            <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Créneaux d'entraînement</h3>
            <button onClick={() => { setSlotForm({ jourSemaine: 'LUNDI', heureDebut: '', heureFin: '', categorieId: '', entraineurId: '', terrain: '' }); setEditSlot(null); setShowSlotModal(true); }} className="text-xs font-semibold no-underline cursor-pointer" style={{ color: 'var(--pitch)' }}>+ Ajouter</button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-[13.5px]">
              <thead>
                <tr>
                  {['Jour', 'Horaire', 'Catégorie', 'Entraîneur', 'Terrain', 'Actions'].map(h => (
                    <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-4 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {slotLoading ? (
                  <tr><td colSpan={6} className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Chargement...</td></tr>
                ) : slotsList.length === 0 ? (
                  <tr><td colSpan={6} className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun créneau</td></tr>
                ) : slotsList.map(s => (
                  <tr key={s.id} className="border-b last:border-b-0" style={{ borderColor: '#F0EEE4' }}>
                    <td className="py-3 px-4">{s.jourSemaine?.charAt(0) + s.jourSemaine?.slice(1).toLowerCase()}</td>
                    <td className="py-3 px-4 font-mono text-[13px]">{s.heureDebut}–{s.heureFin}</td>
                    <td className="py-3 px-4">{s.categorieNom || '—'}</td>
                    <td className="py-3 px-4">{s.entraineurPrenom ? `${s.entraineurPrenom} ${s.entraineurNom}` : '—'}</td>
                    <td className="py-3 px-4">{s.terrain || '—'}</td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-1">
                        <button onClick={() => openEditSlot(s)} className="p-1 rounded hover:bg-gray-100" title="Modifier">
                          <Pencil size={13} style={{ color: 'var(--pitch)' }} />
                        </button>
                        <button onClick={() => setDeleteSlotConfirm(s)} className="p-1 rounded hover:bg-red-50" title="Supprimer">
                          <Trash2 size={13} className="text-red" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Categories panel */}
        <div className="panel">
          <div className="flex justify-between items-center px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
            <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Catégories</h3>
            <button onClick={() => { setCatForm({ nom: '', description: '', ageMin: '', ageMax: '' }); setEditCat(null); setShowCatModal(true); }} className="text-xs font-semibold no-underline cursor-pointer" style={{ color: 'var(--pitch)' }}>+ Ajouter</button>
          </div>
          <div className="py-1">
            {catLoading ? (
              <div className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Chargement...</div>
            ) : cats.length === 0 ? (
              <div className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Aucune catégorie</div>
            ) : cats.map(c => (
              <div key={c.id} className="flex gap-3 px-5 py-3 border-b last:border-b-0 items-center" style={{ borderColor: '#F0EEE4' }}>
                <div className="flex-1">
                  <div className="text-sm font-semibold">{c.nom}</div>
                  <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>{c.description || `Âge ${c.ageMin || '?'}–${c.ageMax || '?'}`}</div>
                </div>
                <button onClick={() => openEditCat(c)} className="p-1 rounded hover:bg-gray-100" title="Modifier">
                  <Pencil size={13} style={{ color: 'var(--pitch)' }} />
                </button>
                <button onClick={() => setDeleteCatConfirm(c)} className="p-1 rounded hover:bg-red-50" title="Supprimer">
                  <Trash2 size={13} className="text-red" />
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Create/Edit Category Modal */}
      {(showCatModal || editCat) && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>{editCat ? 'Modifier la catégorie' : 'Nouvelle catégorie'}</h3>
              <button onClick={() => { setShowCatModal(false); setEditCat(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div>
                <label className="label">Nom de la catégorie</label>
                <input value={catForm.nom} onChange={e => setCatForm({...catForm, nom: e.target.value})} className="input-field" placeholder="Ex. U11" required />
              </div>
              <div>
                <label className="label">Description / tranche d'âge</label>
                <input value={catForm.description} onChange={e => setCatForm({...catForm, description: e.target.value})} className="input-field" placeholder="Ex. Nés en 2014-2015 · Formation" />
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Âge min</label>
                  <input type="number" value={catForm.ageMin} onChange={e => setCatForm({...catForm, ageMin: e.target.value})} className="input-field" />
                </div>
                <div className="flex-1">
                  <label className="label">Âge max</label>
                  <input type="number" value={catForm.ageMax} onChange={e => setCatForm({...catForm, ageMax: e.target.value})} className="input-field" />
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowCatModal(false); setEditCat(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={handleCatSubmit}
                disabled={!catForm.nom || createCatMutation.isPending || updateCatMutation.isPending}
                className="btn-primary text-sm"
              >
                {(createCatMutation.isPending || updateCatMutation.isPending) ? 'Enregistrement...' : editCat ? 'Mettre à jour' : 'Ajouter la catégorie'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create/Edit Slot Modal */}
      {(showSlotModal || editSlot) && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>{editSlot ? 'Modifier le créneau' : 'Nouveau créneau d\'entraînement'}</h3>
              <button onClick={() => { setShowSlotModal(false); setEditSlot(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Jour</label>
                  <select value={slotForm.jourSemaine} onChange={e => setSlotForm({...slotForm, jourSemaine: e.target.value})} className="input-field" required>
                    {DAYS.map(d => <option key={d} value={d}>{d.charAt(0) + d.slice(1).toLowerCase()}</option>)}
                  </select>
                </div>
                <div className="flex-1">
                  <label className="label">Catégorie *</label>
                  <select value={slotForm.categorieId} onChange={e => setSlotForm({...slotForm, categorieId: e.target.value})} className="input-field" required>
                    <option value="">Choisir...</option>
                    {cats.map(c => <option key={c.id} value={c.id}>{c.nom}</option>)}
                  </select>
                </div>
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Heure de début *</label>
                  <input type="time" value={slotForm.heureDebut} onChange={e => setSlotForm({...slotForm, heureDebut: e.target.value})} className="input-field" required />
                </div>
                <div className="flex-1">
                  <label className="label">Heure de fin *</label>
                  <input type="time" value={slotForm.heureFin} onChange={e => setSlotForm({...slotForm, heureFin: e.target.value})} className="input-field" required />
                </div>
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Entraîneur *</label>
                  <select value={slotForm.entraineurId} onChange={e => setSlotForm({...slotForm, entraineurId: e.target.value})} className="input-field" required>
                    <option value="">Choisir un entraîneur...</option>
                    {coachList.map(c => <option key={c.id} value={c.id}>{c.prenom} {c.nom}</option>)}
                  </select>
                </div>
                <div className="flex-1">
                  <label className="label">Terrain / salle</label>
                  <select value={slotForm.terrain} onChange={e => setSlotForm({...slotForm, terrain: e.target.value})} className="input-field">
                    <option value="">Choisir...</option>
                    {TERRAINS.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowSlotModal(false); setEditSlot(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={handleSlotSubmit}
                disabled={!slotForm.heureDebut || !slotForm.heureFin || !slotForm.categorieId || !slotForm.entraineurId || createSlotMutation.isPending || updateSlotMutation.isPending}
                className="btn-primary text-sm"
              >
                {(createSlotMutation.isPending || updateSlotMutation.isPending) ? 'Enregistrement...' : editSlot ? 'Mettre à jour' : 'Ajouter le créneau'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Category Confirm */}
      {deleteCatConfirm && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Supprimer cette catégorie ?</h3>
              <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
                Voulez-vous vraiment supprimer <strong>{deleteCatConfirm.nom}</strong> ?
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setDeleteCatConfirm(null)} className="btn-ghost text-sm">Annuler</button>
              <button onClick={() => deleteCatMutation.mutate(deleteCatConfirm.id)} disabled={deleteCatMutation.isPending} className="btn-danger text-sm">
                {deleteCatMutation.isPending ? 'Suppression...' : 'Supprimer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Slot Confirm */}
      {deleteSlotConfirm && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Supprimer ce créneau ?</h3>
              <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
                {deleteSlotConfirm.jourSemaine} {deleteSlotConfirm.heureDebut}–{deleteSlotConfirm.heureFin} — {deleteSlotConfirm.categorieNom || '—'}
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setDeleteSlotConfirm(null)} className="btn-ghost text-sm">Annuler</button>
              <button onClick={() => deleteSlotMutation.mutate(deleteSlotConfirm.id)} disabled={deleteSlotMutation.isPending} className="btn-danger text-sm">
                {deleteSlotMutation.isPending ? 'Suppression...' : 'Supprimer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
