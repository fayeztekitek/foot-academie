import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { playersApi, categoriesApi, parentsApi, presencesApi } from '../api';
import { Search, Plus, X, Pencil, Trash2, Check, FileCheck } from 'lucide-react';
import { PhotoUpload, PhotoAvatar } from '../components/PhotoUpload';

const FREQ_LABELS = { MENSUEL: 'Mensuel', TRIMESTRIEL: 'Trimestriel', SEMESTRIEL: 'Semestriel', ANNUEL: 'Annuel' };
const FREQ_AMOUNTS = { MENSUEL: '60', TRIMESTRIEL: '180', SEMESTRIEL: '360', ANNUEL: '720' };

function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

function getBirthYear(dateStr) {
  if (!dateStr) return '—';
  return dateStr.split('-')[0];
}

function PaymentPill({ statut }) {
  const map = {
    A_JOUR: { label: 'À jour', cls: 'pill-ok' },
    EN_RETARD: { label: 'Retard', cls: 'pill-retard' },
    PAYE: { label: 'À jour', cls: 'pill-ok' },
    EN_ATTENTE: { label: 'En attente', cls: 'pill-attente' },
  };
  const s = map[statut] || { label: statut || '—', cls: 'pill-attente' };
  return <span className={s.cls}>{s.label}</span>;
}

const EMPTY_FORM = { prenom: '', nom: '', dateNaissance: '', dateEntree: '', categorieId: '', parentId: '', frequence: 'MENSUEL', certificatMedical: false, autorisationParentale: false, photoUrl: '' };

export default function Players() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [catFilter, setCatFilter] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(null);
  const [showFreqModal, setShowFreqModal] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const { data, isLoading } = useQuery({
    queryKey: ['players', page, search, catFilter],
    queryFn: () => playersApi.getAll({ page, size: 20, search, categorieId: catFilter || undefined }).then(r => r.data),
  });

  const { data: categories } = useQuery({
    queryKey: ['categories-list'],
    queryFn: () => categoriesApi.getAll({ size: 50 }).then(r => r.data?.content || r.data),
  });

  const { data: parents } = useQuery({
    queryKey: ['parents-list'],
    queryFn: () => parentsApi.getAll({ size: 100 }).then(r => r.data?.content || r.data),
  });

  const { data: allAttStats } = useQuery({
    queryKey: ['attendance-all'],
    queryFn: () => presencesApi.getAllJoueurStats().then(r => r.data || []),
  });

  const attMap = {};
  if (Array.isArray(allAttStats)) {
    allAttStats.forEach(s => { attMap[s.joueurId] = s.mois; });
  }

  const createMutation = useMutation({
    mutationFn: (data) => playersApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['players']);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => playersApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['players']);
      setShowEditModal(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => playersApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['players']);
      setShowDeleteConfirm(null);
    },
  });

  const freqMutation = useMutation({
    mutationFn: ({ id, frequence }) => playersApi.changeFrequency(id, frequence),
    onSuccess: () => {
      queryClient.invalidateQueries(['players']);
      setShowFreqModal(null);
    },
  });

  const players = data?.content || data || [];
  const totalPages = data?.totalPages || 1;
  const displayCategories = Array.isArray(categories) ? categories : [];
  const displayParents = Array.isArray(parents) ? parents : [];

  const openEdit = (p) => {
    setForm({
      prenom: p.prenom || '',
      nom: p.nom || '',
      dateNaissance: p.dateNaissance || '',
      dateEntree: p.dateEntree || '',
      categorieId: p.categorieId || '',
      parentId: p.parentId || '',
      frequence: p.frequence || 'MENSUEL',
      certificatMedical: p.certificatMedical || false,
      autorisationParentale: p.autorisationParentale || false,
      photoUrl: p.photoUrl || '',
    });
    setShowEditModal(p);
  };

  return (
    <div>
      {/* Toolbar */}
      <div className="flex justify-between items-center mb-5 gap-3.5 flex-wrap">
        <div className="flex-1 max-w-[340px] relative">
          <Search size={15} className="absolute left-3 top-3" style={{ color: 'var(--ink-soft)' }} />
          <input
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            placeholder="Rechercher un joueur…"
            className="input-field pl-9"
            aria-label="Rechercher un joueur"
          />
        </div>
        <div className="flex gap-2 overflow-x-auto pb-0.5">
          <button
            onClick={() => { setCatFilter(''); setPage(0); }}
            className={`chip text-xs font-semibold px-3 py-2 rounded-full border cursor-pointer ${!catFilter ? 'bg-pitch-dark border-pitch-dark text-white' : 'bg-white border-line text-ink-soft'}`}
          >
            Tous
          </button>
          {displayCategories.map(c => (
            <button
              key={c.id}
              onClick={() => { setCatFilter(c.id); setPage(0); }}
              className={`chip text-xs font-semibold px-3 py-2 rounded-full border cursor-pointer whitespace-nowrap ${catFilter === c.id ? 'bg-pitch-dark border-pitch-dark text-white' : 'bg-white border-line text-ink-soft'}`}
            >
              {c.nom}
            </button>
          ))}
        </div>
        <button onClick={() => { setForm(EMPTY_FORM); setShowCreateModal(true); }} className="btn-primary flex items-center gap-1.5 text-sm whitespace-nowrap" aria-label="Créer un nouveau joueur">
          <Plus size={15} /> Nouveau joueur
        </button>
      </div>

      {/* Table */}
      <div className="panel">
        <div className="overflow-x-auto">
          <table className="w-full text-[13.5px] table-responsive">
            <thead>
              <tr>
                {['Joueur', 'Date entrée', 'Catégorie', 'Parent', 'Paiements', 'Présence', 'Conformité', 'Fréquence', 'Actions'].map(h => (
                  <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-5 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}><td colSpan={9} className="py-3 px-5"><div className="skeleton-row" /></td></tr>
                ))
              ) : players.length === 0 ? (
                <tr><td colSpan={9} className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun joueur trouvé</td></tr>
              ) : players.map(p => (
                <tr key={p.id} className="border-b last:border-b-0 hover:bg-gray-50/50 transition-colors" style={{ borderColor: '#F0EEE4' }}>
                  <td className="py-3 px-5" data-label="Joueur">
                    <div className="flex items-center gap-2.5">
                      <PhotoAvatar photoUrl={p.photoUrl} prenom={p.prenom} nom={p.nom} />
                      <div>
                        <div className="text-[13.5px] font-semibold">{p.prenom} {p.nom}</div>
                        <div className="text-[11.5px]" style={{ color: 'var(--ink-soft)' }}>Né en {getBirthYear(p.dateNaissance)}</div>
                      </div>
                    </div>
                  </td>
                  <td className="py-3 px-5 text-[12px]" data-label="Date entrée">
                    {p.dateEntree ? (
                      <div>
                        <div className="font-medium">{new Date(p.dateEntree).toLocaleDateString('fr-FR', { month: 'short', year: 'numeric' })}</div>
                      </div>
                    ) : '—'}
                  </td>
                  <td className="py-3 px-5" data-label="Catégorie">{p.categorieNom || '—'}</td>
                  <td className="py-3 px-5" data-label="Parent">{p.parentPrenom ? `${p.parentPrenom} ${p.parentNom}` : '—'}</td>
                  <td className="py-3 px-5" data-label="Paiements">
                    <div className="flex flex-col gap-0.5 text-[11.5px]">
                      <div className="flex items-center gap-1">
                        <span className="font-semibold" style={{ color: 'var(--ink-dark)' }}>{p.moisPayes || 0}</span>
                        <span style={{ color: 'var(--ink-soft)' }}>/ {p.moisAVerser || 0} mois payés</span>
                      </div>
                      {p.moisImpayes > 0 && (
                        <span className="font-semibold text-red">{p.moisImpayes} mois impayés</span>
                      )}
                    </div>
                  </td>
                  <td className="py-3 px-5" data-label="Présence">
                    {attMap[p.id] ? (
                      <div className="flex flex-col gap-0.5">
                        <span className="font-semibold text-[13px]" style={{ color: (attMap[p.id].attendanceRate || 0) >= 80 ? 'var(--grass)' : (attMap[p.id].attendanceRate || 0) >= 60 ? 'var(--gold)' : 'var(--red)' }}>
                          {(attMap[p.id].attendanceRate || 0).toFixed(1)}%
                        </span>
                        <span className="text-[11px]" style={{ color: 'var(--ink-soft)' }}>
                          {attMap[p.id].presentCount || 0}/{attMap[p.id].totalSessions || 0} séances
                        </span>
                      </div>
                    ) : (
                      <span className="text-[12px]" style={{ color: 'var(--ink-soft)' }}>—</span>
                    )}
                  </td>
                  <td className="py-3 px-5" data-label="Conformité">
                    <div className="flex flex-col gap-0.5">
                      <span className={`text-[11px] font-semibold flex items-center gap-1 ${p.certificatMedical ? 'text-grass' : 'text-red'}`}>
                        <FileCheck size={11} /> Cert. médical
                      </span>
                      <span className={`text-[11px] font-semibold flex items-center gap-1 ${p.autorisationParentale ? 'text-grass' : 'text-red'}`}>
                        <Check size={11} /> Autorisation
                      </span>
                    </div>
                  </td>
                  <td className="py-3 px-5" data-label="Fréquence">
                    <button onClick={() => setShowFreqModal(p)} className="text-xs font-medium underline cursor-pointer" style={{ color: 'var(--pitch)' }}>
                      {FREQ_LABELS[p.frequence] || p.frequence || '—'}
                    </button>
                  </td>
                  <td className="py-3 px-5" data-label="Actions">
                    <div className="flex items-center">
                      <button onClick={() => openEdit(p)} className="icon-btn" title="Modifier" aria-label={`Modifier ${p.prenom} ${p.nom}`}>
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => setShowDeleteConfirm(p)} className="icon-btn text-red" title="Supprimer" aria-label={`Supprimer ${p.prenom} ${p.nom}`}>
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

      {/* Player Form Modal (Create/Edit) */}
      {(showCreateModal || showEditModal) && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden max-h-[90vh] flex flex-col">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>
                {showEditModal ? 'Modifier le joueur' : 'Nouveau joueur'}
              </h3>
              <button onClick={() => { setShowCreateModal(false); setShowEditModal(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5 overflow-y-auto">
              <PhotoUpload
                value={form.photoUrl}
                onChange={(v) => setForm({...form, photoUrl: v || ''})}
                prenom={form.prenom}
                nom={form.nom}
              />
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
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Date de naissance</label>
                  <input type="date" value={form.dateNaissance} onChange={e => setForm({...form, dateNaissance: e.target.value})} className="input-field" required />
                </div>
                <div className="flex-1">
                  <label className="label">Date d'entrée à l'académie</label>
                  <input type="date" value={form.dateEntree} onChange={e => setForm({...form, dateEntree: e.target.value})} className="input-field" required />
                </div>
              </div>
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Catégorie</label>
                  <select value={form.categorieId} onChange={e => setForm({...form, categorieId: e.target.value})} className="input-field" required>
                    <option value="">Choisir...</option>
                    {displayCategories.map(c => <option key={c.id} value={c.id}>{c.nom}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="label">Parent référent</label>
                <select value={form.parentId} onChange={e => setForm({...form, parentId: e.target.value})} className="input-field" required>
                  <option value="">Choisir un parent...</option>
                  {displayParents.map(p => <option key={p.id} value={p.id}>{p.prenom} {p.nom}</option>)}
                </select>
              </div>
              <div>
                <label className="label">Fréquence de paiement</label>
                <select value={form.frequence} onChange={e => setForm({...form, frequence: e.target.value})} className="input-field">
                  <option value="MENSUEL">Mensuel — 60 TND/mois</option>
                  <option value="TRIMESTRIEL">Trimestriel — 180 TND/trimestre</option>
                  <option value="SEMESTRIEL">Semestriel — 360 TND/semestre</option>
                  <option value="ANNUEL">Annuel — 720 TND/an</option>
                </select>
              </div>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.certificatMedical}
                    onChange={e => setForm({...form, certificatMedical: e.target.checked})}
                    className="w-4 h-4 rounded accent-[var(--pitch)]"
                  />
                  <span>Certificat médical</span>
                </label>
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.autorisationParentale}
                    onChange={e => setForm({...form, autorisationParentale: e.target.checked})}
                    className="w-4 h-4 rounded accent-[var(--pitch)]"
                  />
                  <span>Autorisation parentale</span>
                </label>
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowCreateModal(false); setShowEditModal(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => {
                  if (showEditModal) {
                    updateMutation.mutate({ id: showEditModal.id, data: form });
                  } else {
                    createMutation.mutate(form);
                  }
                }}
                disabled={createMutation.isPending || updateMutation.isPending}
                className="btn-primary text-sm"
              >
                {(createMutation.isPending || updateMutation.isPending)
                  ? 'Enregistrement...'
                  : showEditModal ? 'Mettre à jour' : 'Enregistrer le joueur'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Frequency Modal */}
      {showFreqModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Fréquence — {showFreqModal.prenom} {showFreqModal.nom}</h3>
              <button onClick={() => setShowFreqModal(null)} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-2">
              {Object.entries(FREQ_LABELS).map(([key, label]) => (
                <button
                  key={key}
                  onClick={() => freqMutation.mutate({ id: showFreqModal.id, frequence: key })}
                  disabled={freqMutation.isPending}
                  className={`w-full text-left px-4 py-3 rounded-lg border text-sm font-medium transition-colors ${
                    showFreqModal.frequence === key
                      ? 'bg-pitch-dark border-pitch-dark text-white'
                      : 'bg-white border-line hover:bg-gray-50'
                  }`}
                >
                  {label} — {FREQ_AMOUNTS[key]} TND
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Supprimer ce joueur ?</h3>
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
    </div>
  );
}
