import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { coachesApi, categoriesApi } from '../api';
import { Search, Plus, X, Pencil, Trash2 } from 'lucide-react';

function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

const EMPTY_FORM = { prenom: '', nom: '', specialite: '', telephone: '', email: '', categorieIds: [] };

export default function Coaches() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [createdCredentials, setCreatedCredentials] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['coaches', page, search],
    queryFn: () => coachesApi.getAll({ page, size: 20, search }).then(r => r.data),
  });

  const { data: categories } = useQuery({
    queryKey: ['categories-list-coach'],
    queryFn: () => categoriesApi.getAll({ size: 50 }).then(r => r.data?.content || r.data),
  });

  const createMutation = useMutation({
    mutationFn: (data) => coachesApi.create(data),
    onSuccess: (response) => {
      queryClient.invalidateQueries(['coaches']);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
      if (response.data?.motDePasse) {
        setCreatedCredentials({
          email: form.email,
          motDePasse: response.data.motDePasse,
        });
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => coachesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['coaches']);
      setShowEditModal(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => coachesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['coaches']);
      setShowDeleteConfirm(null);
    },
  });

  const coaches = data?.content || data || [];
  const totalPages = data?.totalPages || 1;
  const displayCategories = Array.isArray(categories) ? categories : [];

  const openEdit = (c) => {
    setForm({
      prenom: c.prenom || '',
      nom: c.nom || '',
      specialite: c.specialite || '',
      telephone: c.telephone || '',
      email: c.email || '',
      categorieIds: c.categories?.map(cat => cat.id) || [],
    });
    setShowEditModal(c);
  };

  const handleSubmit = () => {
    if (showEditModal) {
      updateMutation.mutate({ id: showEditModal.id, data: form });
    } else {
      createMutation.mutate(form);
    }
  };

  const toggleCat = (id) => {
    setForm(f => ({
      ...f,
      categorieIds: f.categorieIds.includes(id)
        ? f.categorieIds.filter(c => c !== id)
        : [...f.categorieIds, id],
    }));
  };

  return (
    <div>
      {/* Toolbar */}
      <div className="flex justify-between items-center mb-5 gap-3.5">
        <div className="flex-1 max-w-[340px] relative">
          <Search size={15} className="absolute left-3 top-3" style={{ color: 'var(--ink-soft)' }} />
          <input
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            placeholder="Rechercher un entraîneur…"
            className="input-field pl-9"
          />
        </div>
        <button onClick={() => { setForm(EMPTY_FORM); setShowCreateModal(true); }} className="btn-primary flex items-center gap-1.5 text-sm">
          <Plus size={15} /> Nouvel entraîneur
        </button>
      </div>

      {/* Table */}
      <div className="panel">
        <div className="overflow-x-auto">
          <table className="w-full text-[13.5px] table-responsive">
            <thead>
              <tr>
                {['Entraîneur', 'Spécialité', 'Catégories', 'Téléphone', 'Actions'].map(h => (
                  <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-5 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                Array.from({ length: 4 }).map((_, i) => (
                  <tr key={i}><td colSpan={5} className="py-3 px-5"><div className="skeleton-row" /></td></tr>
                ))
              ) : coaches.length === 0 ? (
                <tr><td colSpan={5} className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun entraîneur trouvé</td></tr>
              ) : coaches.map(c => (
                <tr key={c.id} className="border-b last:border-b-0 hover:bg-gray-50/50 transition-colors" style={{ borderColor: '#F0EEE4' }}>
                  <td className="py-3 px-5" data-label="Entraîneur">
                    <div className="flex items-center gap-2.5">
                      <div className="p-avatar">{getInitials(c.prenom, c.nom)}</div>
                      <div>
                        <div className="text-[13.5px] font-semibold">{c.prenom} {c.nom}</div>
                        <div className="text-[11.5px]" style={{ color: 'var(--ink-soft)' }}>{c.specialite || '—'}</div>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-5" data-label="Spécialité">{c.specialite || '—'}</td>
                  <td className="py-3 px-5" data-label="Catégories">
                    <div className="flex gap-1 flex-wrap">
                      {c.categories?.map(cat => (
                        <span key={cat.id} className="pill bg-pitch-dark text-white text-[11px]">{cat.nom}</span>
                      ))}
                    </div>
                  </td>
                  <td className="py-3 px-5 font-mono text-[13px]" data-label="Téléphone">{c.telephone || '—'}</td>
                  <td className="py-3 px-5" data-label="Actions">
                    <div className="flex items-center">
                      <button onClick={() => openEdit(c)} className="icon-btn" title="Modifier" aria-label={`Modifier ${c.prenom} ${c.nom}`}>
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => setShowDeleteConfirm(c)} className="icon-btn text-red" title="Supprimer" aria-label={`Supprimer ${c.prenom} ${c.nom}`}>
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="flex justify-center gap-2 py-4 border-t" style={{ borderColor: 'var(--line)' }}>
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="btn-ghost text-xs">← Précédent</button>
            <span className="text-xs py-2 px-3" style={{ color: 'var(--ink-soft)' }}>Page {page + 1} / {totalPages}</span>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="btn-ghost text-xs">Suivant →</button>
          </div>
        )}
      </div>

      {/* Coach Form Modal (Create/Edit) */}
      {(showCreateModal || showEditModal) && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden max-h-[90vh] flex flex-col">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>
                {showEditModal ? 'Modifier l\'entraîneur' : 'Nouvel entraîneur'}
              </h3>
              <button onClick={() => { setShowCreateModal(false); setShowEditModal(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5 overflow-y-auto">
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Prénom</label>
                  <input value={form.prenom} onChange={e => setForm({...form, prenom: e.target.value})} className="input-field" required />
                </div>
                <div className="flex-1">
                  <label className="label">Nom</label>
                  <input value={form.nom} onChange={e => setForm({...form, nom: e.target.value})} className="input-field" required />
                </div>
              </div>
              <div>
                <label className="label">Spécialité / diplôme</label>
                <input value={form.specialite} onChange={e => setForm({...form, specialite: e.target.value})} className="input-field" placeholder="Ex. CAF B, entraîneur gardiens…" />
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Téléphone</label>
                  <input value={form.telephone} onChange={e => setForm({...form, telephone: e.target.value})} className="input-field" placeholder="+216 XX XXX XXX" />
                </div>
                <div className="flex-1">
                  <label className="label">Email</label>
                  <input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} className="input-field" />
                </div>
              </div>
              <div>
                <label className="label">Catégories encadrées</label>
                <div className="flex flex-col gap-1.5">
                  {displayCategories.map(c => (
                    <label key={c.id} className="flex items-center gap-2 text-sm cursor-pointer">
                      <input
                        type="checkbox"
                        checked={form.categorieIds.includes(c.id)}
                        onChange={() => toggleCat(c.id)}
                        className="w-4 h-4"
                      />
                      {c.nom}
                    </label>
                  ))}
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowCreateModal(false); setShowEditModal(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={handleSubmit}
                disabled={(!form.prenom || !form.nom) || createMutation.isPending || updateMutation.isPending}
                className="btn-primary text-sm"
              >
                {(createMutation.isPending || updateMutation.isPending)
                  ? 'Enregistrement...'
                  : showEditModal ? 'Mettre à jour' : 'Enregistrer l\'entraîneur'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Supprimer cet entraîneur ?</h3>
              <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
                Voulez-vous vraiment supprimer <strong>{showDeleteConfirm.prenom} {showDeleteConfirm.nom}</strong> ? Cette action est irréversible.
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setShowDeleteConfirm(null)} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => deleteMutation.mutate(showDeleteConfirm.id)}
                disabled={deleteMutation.isPending}
                className="btn-danger text-sm"
              >
                {deleteMutation.isPending ? 'Suppression...' : 'Supprimer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Credentials Modal */}
      {createdCredentials && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[400px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Compte créé avec succès</h3>
              <p className="text-sm mb-4" style={{ color: 'var(--ink-soft)' }}>Communiquez ces identifiants à l'entraîneur :</p>
              <div className="p-4 rounded-lg" style={{ background: '#F0EEE4' }}>
                <div className="text-sm"><strong>Email :</strong> <span className="font-mono">{createdCredentials.email}</span></div>
                <div className="text-sm mt-1"><strong>Mot de passe :</strong> <span className="font-mono font-bold" style={{ color: 'var(--pitch-dark)' }}>{createdCredentials.motDePasse}</span></div>
              </div>
            </div>
            <div className="flex justify-end px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setCreatedCredentials(null)} className="btn-primary text-sm">Fermer</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
