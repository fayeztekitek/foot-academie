import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { parentsApi } from '../api';
import { Search, Plus, X, Pencil, Trash2, Key, MessageCircle } from 'lucide-react';

function getInitials(prenom, nom) {
  return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
}

const EMPTY_FORM = { prenom: '', nom: '', telephone: '', email: '', motDePasse: '', consentementRGPD: true };

export default function Parents() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [createdCredentials, setCreatedCredentials] = useState(null);
  const [showResetModal, setShowResetModal] = useState(null);
  const [resetPassword, setResetPassword] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['parents', page, search],
    queryFn: () => parentsApi.getAll({ page, size: 20, search }).then(r => r.data),
  });

  const createMutation = useMutation({
    mutationFn: (data) => parentsApi.create(data),
    onSuccess: (response) => {
      queryClient.invalidateQueries(['parents']);
      setShowCreateModal(false);
      setForm(EMPTY_FORM);
      if (response.data?.motDePasse) {
        setCreatedCredentials({
          prenom: form.prenom,
          nom: form.nom,
          telephone: form.telephone,
          email: form.email,
          motDePasse: response.data.motDePasse,
        });
      }
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => parentsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['parents']);
      setShowEditModal(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => parentsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['parents']);
      setShowDeleteConfirm(null);
    },
  });

  const resetPasswordMutation = useMutation({
    mutationFn: ({ id, motDePasse }) => parentsApi.resetPassword(id, motDePasse),
    onSuccess: () => {
      queryClient.invalidateQueries(['parents']);
      setShowResetModal(null);
      setResetPassword('');
    },
  });

  const parents = data?.content || data || [];
  const totalPages = data?.totalPages || 1;

  const openEdit = (p) => {
    setForm({
      prenom: p.prenom || '',
      nom: p.nom || '',
      telephone: p.telephone || '',
      email: p.email || '',
      consentementRGPD: p.consentementRGPD ?? true,
    });
    setShowEditModal(p);
  };

  const handleSubmit = () => {
    if (showEditModal) {
      updateMutation.mutate({ id: showEditModal.id, data: form });
    } else {
      createMutation.mutate(form);
    }
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
            placeholder="Rechercher un parent…"
            className="input-field pl-9"
          />
        </div>
        <button onClick={() => { setForm(EMPTY_FORM); setShowCreateModal(true); }} className="btn-primary flex items-center gap-1.5 text-sm">
          <Plus size={15} /> Nouveau parent
        </button>
      </div>

      {/* Table */}
      <div className="panel">
        <div className="overflow-x-auto">
          <table className="w-full text-[13.5px] table-responsive">
            <thead>
              <tr>
                {['Parent', 'Téléphone', 'Email', 'Enfants', 'RGPD', 'Actions'].map(h => (
                  <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-5 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                Array.from({ length: 4 }).map((_, i) => (
                  <tr key={i}><td colSpan={6} className="py-3 px-5"><div className="skeleton-row" /></td></tr>
                ))
              ) : parents.length === 0 ? (
                <tr><td colSpan={6} className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun parent trouvé</td></tr>
              ) : parents.map(p => (
                <tr key={p.id} className="border-b last:border-b-0 hover:bg-gray-50/50 transition-colors" style={{ borderColor: '#F0EEE4' }}>
                  <td className="py-3 px-5" data-label="Parent">
                    <div className="flex items-center gap-2.5">
                      <div className="p-avatar">{getInitials(p.prenom, p.nom)}</div>
                      <div className="text-[13.5px] font-medium">{p.prenom} {p.nom}</div>
                    </div>
                  </td>
                  <td className="py-3 px-5 font-mono text-[13px]" data-label="Téléphone">{p.telephone || '—'}</td>
                  <td className="py-3 px-5" data-label="Email">{p.email || '—'}</td>
                  <td className="py-3 px-5" data-label="Enfants">{p.joueurs?.length || 0}</td>
                  <td className="py-3 px-5" data-label="RGPD">
                    {p.consentementRGPD ? (
                      <span className="pill-ok pill">Consenti</span>
                    ) : (
                      <span className="bg-gray-100 text-gray-600 pill">Non</span>
                    )}
                  </td>
                  <td className="py-3 px-5" data-label="Actions">
                    <div className="flex items-center">
                      <button onClick={() => openEdit(p)} className="icon-btn" title="Modifier" aria-label={`Modifier ${p.prenom} ${p.nom}`}>
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => { setShowResetModal(p); setResetPassword(''); }} className="icon-btn text-blue-600" title="Réinitialiser le mot de passe" aria-label={`Réinitialiser le mot de passe de ${p.prenom} ${p.nom}`}>
                        <Key size={16} />
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

      {/* Parent Form Modal (Create/Edit) */}
      {(showCreateModal || showEditModal) && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden max-h-[90vh] flex flex-col">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>
                {showEditModal ? 'Modifier le parent' : 'Nouveau parent'}
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
              {!showEditModal && (
                <div>
                  <label className="label">Mot de passe (optionnel — sinon généré automatiquement)</label>
                  <input type="password" value={form.motDePasse} onChange={e => setForm({...form, motDePasse: e.target.value})} className="input-field" placeholder="Min. 6 caractères" autoComplete="new-password" />
                </div>
              )}
              <label className="flex items-center gap-2 text-sm cursor-pointer">
                <input type="checkbox" checked={form.consentementRGPD} onChange={e => setForm({...form, consentementRGPD: e.target.checked})} className="w-4 h-4" />
                Consentement RGPD / droit à l'image donné
              </label>
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
                  : showEditModal ? 'Mettre à jour' : 'Enregistrer le parent'}
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
              <h3 className="font-bebas text-xl mb-2" style={{ color: 'var(--pitch-dark)' }}>Supprimer ce parent ?</h3>
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
              <p className="text-sm mb-4" style={{ color: 'var(--ink-soft)' }}>Communiquez ces identifiants au parent :</p>
              <div className="p-4 rounded-lg" style={{ background: '#F0EEE4' }}>
                <div className="text-sm"><strong>Email :</strong> <span className="font-mono">{createdCredentials.email}</span></div>
                <div className="text-sm mt-1"><strong>Mot de passe :</strong> <span className="font-mono font-bold" style={{ color: 'var(--pitch-dark)' }}>{createdCredentials.motDePasse}</span></div>
              </div>
            </div>
            <div className="flex justify-end gap-2 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              {createdCredentials.telephone && (
                <a
                  href={`https://wa.me/${createdCredentials.telephone.replace(/[^0-9]/g, '')}?text=${encodeURIComponent(
                    `Bonjour ${createdCredentials.prenom} ${createdCredentials.nom},\n\n` +
                    `Votre compte Nadi Académie a été créé.\n\n` +
                    `Email : ${createdCredentials.email}\n\n` +
                    `Pour des raisons de sécurité, le mot de passe temporaire vous sera communiqué séparément ` +
                    `et vous devrez le changer lors de votre première connexion.\n\n` +
                    `Connectez-vous sur : https://frontend-theta-navy-p2kodej171.vercel.app/login`
                  )}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-white"
                  style={{ background: '#25D366' }}
                >
                  <MessageCircle size={16} />
                  WhatsApp
                </a>
              )}
              <button onClick={() => setCreatedCredentials(null)} className="btn-primary text-sm">Fermer</button>
            </div>
          </div>
        </div>
      )}

      {/* Reset Password Modal */}
      {showResetModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[400px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Réinitialiser le mot de passe</h3>
              <button onClick={() => setShowResetModal(null)} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div className="p-3 rounded-lg text-sm" style={{ background: '#F0EEE4' }}>
                <div><strong>{showResetModal.prenom} {showResetModal.nom}</strong></div>
                <div style={{ color: 'var(--ink-soft)' }}>{showResetModal.email}</div>
              </div>
              <div>
                <label className="label">Nouveau mot de passe</label>
                <input
                  type="password"
                  value={resetPassword}
                  onChange={e => setResetPassword(e.target.value)}
                  className="input-field"
                  placeholder="Min. 6 caractères"
                  autoComplete="new-password"
                  autoFocus
                />
              </div>
              <p className="text-[11px]" style={{ color: 'var(--ink-soft)' }}>
                Le parent devra changer son mot de passe lors de sa prochaine connexion.
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setShowResetModal(null)} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => resetPasswordMutation.mutate({ id: showResetModal.id, motDePasse: resetPassword })}
                disabled={!resetPassword || resetPassword.length < 6 || resetPasswordMutation.isPending}
                className="btn-primary text-sm"
              >
                {resetPasswordMutation.isPending ? 'Enregistrement...' : 'Réinitialiser'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
