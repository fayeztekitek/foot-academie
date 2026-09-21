import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentsApi, playersApi, reportsApi } from '../api';
import { Search, Plus, X, Download, Trash2 } from 'lucide-react';

const STATUT_LABELS = {
  EN_ATTENTE: 'En attente',
  PAYE: 'Payé',
  EN_RETARD: 'En retard',
  ANNULE: 'Annulé',
};
const STATUT_PILL = {
  EN_ATTENTE: 'pill-attente',
  PAYE: 'pill-ok',
  EN_RETARD: 'pill-retard',
  ANNULE: 'bg-gray-100 text-gray-600',
};
const MOYEN_LABELS = {
  ESPECES: 'Espèces',
  VIREMENT: 'Virement',
  CARTE_BANCAIRE: 'Carte bancaire',
  EN_LIGNE: 'En ligne',
};
const FREQ_LABELS = { MENSUEL: 'Mensuel', TRIMESTRIEL: 'Trimestriel', SEMESTRIEL: 'Semestriel', ANNUEL: 'Annuel' };
const FREQ_AMOUNTS = { MENSUEL: 60, TRIMESTRIEL: 180, SEMESTRIEL: 360, ANNUEL: 720 };

const MONTHS_FR = [
  { key: '2026-01', label: 'Janvier 2026' }, { key: '2026-02', label: 'Février 2026' },
  { key: '2026-03', label: 'Mars 2026' }, { key: '2026-04', label: 'Avril 2026' },
  { key: '2026-05', label: 'Mai 2026' }, { key: '2026-06', label: 'Juin 2026' },
  { key: '2026-07', label: 'Juillet 2026' }, { key: '2026-08', label: 'Août 2026' },
  { key: '2026-09', label: 'Septembre 2026' }, { key: '2026-10', label: 'Octobre 2026' },
  { key: '2026-11', label: 'Novembre 2026' }, { key: '2026-12', label: 'Décembre 2026' },
  { key: '2027-01', label: 'Janvier 2027' }, { key: '2027-02', label: 'Février 2027' },
  { key: '2027-03', label: 'Mars 2027' }, { key: '2027-04', label: 'Avril 2027' },
  { key: '2027-05', label: 'Mai 2027' }, { key: '2027-06', label: 'Juin 2027' },
];

function getCurrentMonthKey() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

export default function Payments() {
  const queryClient = useQueryClient();
  const [tab, setTab] = useState('all');
  const [search, setSearch] = useState('');
  const [moyenFilter, setMoyenFilter] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showExportModal, setShowExportModal] = useState(false);
  const [createForm, setCreateForm] = useState({ joueurId: '', montant: 60, mois: getCurrentMonthKey(), frequence: 'MENSUEL', moyenPaiement: 'ESPECES', statut: 'EN_ATTENTE', commentaire: '' });
  const [showPayModal, setShowPayModal] = useState(false);
  const [payTarget, setPayTarget] = useState(null);
  const [payForm, setPayForm] = useState({ montant: 0, datePaiement: new Date().toISOString().split('T')[0], moyenPaiement: 'ESPECES', commentaire: '' });
  const [exportDates, setExportDates] = useState({ start: '', end: '' });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: ['payments', tab],
    queryFn: () => {
      const params = { page: 0, size: 50 };
      if (tab !== 'all') params.statut = tab;
      return paymentsApi.getAll(params).then(r => r.data);
    },
  });

  const { data: players } = useQuery({
    queryKey: ['players-list-payments'],
    queryFn: () => playersApi.getAll({ size: 200 }).then(r => r.data?.content || r.data),
  });

  const payMutation = useMutation({
    mutationFn: ({ id, data }) => paymentsApi.markAsPaid(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['payments']);
      setShowPayModal(false);
      setPayTarget(null);
    },
  });

  const createMutation = useMutation({
    mutationFn: (data) => paymentsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['payments']);
      setShowCreateModal(false);
      setCreateForm({ joueurId: '', montant: 60, mois: getCurrentMonthKey(), frequence: 'MENSUEL', moyenPaiement: 'ESPECES', statut: 'EN_ATTENTE', commentaire: '' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => paymentsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['payments']);
      setShowDeleteModal(false);
      setDeleteTarget(null);
    },
  });

  const payments = data?.content || data || [];

  const filteredPayments = payments.filter(p => {
    if (search) {
      const q = search.toLowerCase();
      const matchName = `${p.joueurPrenom || ''} ${p.joueurNom || ''}`.toLowerCase().includes(q);
      const matchParent = `${p.parentPrenom || ''} ${p.parentNom || ''}`.toLowerCase().includes(q);
      if (!matchName && !matchParent) return false;
    }
    if (moyenFilter && p.moyenPaiement !== moyenFilter) return false;
    return true;
  });

  const totalFacture = payments.reduce((s, p) => s + (p.montant || 0), 0);
  const totalEncaisse = payments.filter(p => p.statut === 'PAYE').reduce((s, p) => s + (p.montant || 0), 0);
  const totalRetard = payments.filter(p => p.statut === 'EN_RETARD').reduce((s, p) => s + (p.montant || 0), 0);
  const totalEnAttente = payments.filter(p => p.statut === 'EN_ATTENTE').reduce((s, p) => s + (p.montant || 0), 0);

  const selectedPlayer = (Array.isArray(players) ? players : []).find(p => String(p.id) === String(createForm.joueurId));

  const handleExport = async (type) => {
    if (!exportDates.start || !exportDates.end) return;
    try {
      const res = type === 'csv'
        ? await reportsApi.exportCsv(exportDates.start, exportDates.end)
        : await reportsApi.exportExcel(exportDates.start, exportDates.end);
      const blob = new Blob([res.data]);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `paiements-${exportDates.start}-${exportDates.end}.${type === 'csv' ? 'csv' : 'xlsx'}`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Export failed', e);
    }
  };

  return (
    <div>
      {/* Summary stats */}
      <div className="flex gap-px mb-6 border" style={{ borderColor: 'var(--line)', background: 'var(--line)' }}>
        {[
          { label: 'Facturé ce mois', value: `${totalFacture.toLocaleString('fr-TN')} DT`, color: 'var(--pitch-dark)' },
          { label: 'Encaissé', value: `${totalEncaisse.toLocaleString('fr-TN')} DT`, color: 'var(--grass)' },
          { label: 'En retard', value: `${totalRetard.toLocaleString('fr-TN')} DT`, color: 'var(--red)' },
          { label: 'Échéances à venir', value: `${totalEnAttente.toLocaleString('fr-TN')} DT`, color: 'var(--pitch-dark)' },
        ].map((s, i) => (
          <div key={i} className="bg-white flex-1 p-5">
            <div className="text-xs font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>{s.label}</div>
            <div className="font-mono text-[28px] mt-1 leading-none font-medium" style={{ color: s.color }}>{s.value}</div>
          </div>
        ))}
      </div>

      {/* Filter tabs */}
      <div className="flex justify-between items-center mb-5 gap-3 flex-wrap">
        <div className="flex gap-2">
          {[
            { key: 'all', label: 'Tous' },
            { key: 'EN_ATTENTE', label: 'En attente' },
            { key: 'PAYE', label: 'Payés' },
            { key: 'EN_RETARD', label: 'En retard' },
          ].map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`chip text-xs font-semibold px-3 py-2 rounded-full border cursor-pointer ${tab === t.key ? 'bg-pitch-dark border-pitch-dark text-white' : 'bg-white border-line text-ink-soft'}`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Search + payment method filter */}
        <div className="flex gap-3 items-center flex-wrap">
          <div className="relative">
            <Search size={15} className="absolute left-3 top-2.5" style={{ color: 'var(--ink-soft)' }} />
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Rechercher joueur ou parent…"
              className="input-field pl-9 text-xs py-1.5"
              aria-label="Rechercher un paiement"
            />
          </div>
          <select
            value={moyenFilter}
            onChange={e => setMoyenFilter(e.target.value)}
            className="input-field text-xs py-1.5 px-3"
          >
            <option value="">Tous moyens</option>
            <option value="ESPECES">Espèces</option>
            <option value="VIREMENT">Virement</option>
            <option value="CARTE_BANCAIRE">Carte bancaire</option>
            <option value="EN_LIGNE">En ligne</option>
          </select>
          {(search || moyenFilter) && (
            <button
              onClick={() => { setSearch(''); setMoyenFilter(''); }}
              className="text-xs flex items-center gap-1 px-2 py-1 rounded border cursor-pointer hover:bg-gray-50"
              style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}
            >
              <X size={12} /> Effacer
            </button>
          )}
        </div>
        <div className="flex gap-2">
          <button onClick={() => setShowExportModal(true)} className="btn-ghost text-xs flex items-center gap-1">
            <Download size={14} /> Exporter
          </button>
          <button onClick={() => setShowCreateModal(true)} className="btn-primary text-xs flex items-center gap-1">
            <Plus size={14} /> Enregistrer un paiement
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="panel">
        <div className="overflow-x-auto">
          <table className="w-full text-[13.5px] table-responsive">
            <thead>
              <tr>
                {['Joueur', 'Parent', 'Montant', 'Échéance', 'Statut', 'Moyen', 'Formule', 'Actions'].map(h => (
                  <th key={h} className="text-left text-[11.5px] uppercase tracking-wider py-2.5 px-5 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}><td colSpan={8} className="py-3 px-5"><div className="skeleton-row" /></td></tr>
                ))
              ) : payments.length === 0 ? (
                <tr><td colSpan={8} className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun paiement trouvé</td></tr>
              ) : filteredPayments.length === 0 ? (
                <tr><td colSpan={8} className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun paiement ne correspond aux filtres</td></tr>
              ) : filteredPayments.map(p => (
                <tr key={p.id} className="border-b last:border-b-0 hover:bg-gray-50/50 transition-colors" style={{ borderColor: '#F0EEE4' }}>
                  <td className="py-3 px-5" data-label="Joueur">
                    <div className="flex items-center gap-2.5">
                      <div className="p-avatar">{((p.joueurPrenom?.[0] || '') + (p.joueurNom?.[0] || '')).toUpperCase()}</div>
                      <div className="text-[13.5px] font-medium">{p.joueurPrenom} {p.joueurNom}</div>
                    </div>
                  </td>
                  <td className="py-3 px-5" data-label="Parent">{p.parentPrenom ? `${p.parentPrenom} ${p.parentNom}` : '—'}</td>
                  <td className="py-3 px-5 font-mono font-medium" data-label="Montant">{(p.montant || 0).toLocaleString('fr-TN')} DT</td>
                  <td className="py-3 px-5 font-mono" data-label="Échéance">{p.dateEcheance || '—'}</td>
                  <td className="py-3 px-5" data-label="Statut"><span className={`${STATUT_PILL[p.statut] || 'pill-attente'} pill`}>{STATUT_LABELS[p.statut] || p.statut}</span></td>
                  <td className="py-3 px-5" data-label="Moyen">{MOYEN_LABELS[p.moyenPaiement] || '—'}</td>
                  <td className="py-3 px-5" data-label="Formule">{FREQ_LABELS[p.formule] || FREQ_LABELS[p.frequence] || '—'}</td>
                  <td className="py-3 px-5" data-label="Actions">
                    <div className="flex items-center">
                      {p.statut !== 'PAYE' && (
                        <button
                          onClick={() => {
                            setPayTarget(p);
                            setPayForm({
                              montant: p.montant || 60,
                              datePaiement: new Date().toISOString().split('T')[0],
                              moyenPaiement: 'ESPECES',
                              commentaire: '',
                            });
                            setShowPayModal(true);
                          }}
                          className="text-xs font-medium cursor-pointer px-3 py-2 rounded-lg"
                          style={{ color: 'var(--grass)', background: '#E6F1EA', minHeight: '36px' }}
                          aria-label={`Marquer ${p.joueurPrenom} ${p.joueurNom} comme payé`}
                        >
                          Marquer payé
                        </button>
                      )}
                      <button
                        onClick={() => { setDeleteTarget(p); setShowDeleteModal(true); }}
                        className="icon-btn text-red"
                        title="Supprimer"
                        aria-label={`Supprimer le paiement de ${p.joueurPrenom} ${p.joueurNom}`}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Payment Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden max-h-[90vh] flex flex-col">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Enregistrer un paiement</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5 overflow-y-auto">
              <div>
                <label className="label">Joueur</label>
                <select value={createForm.joueurId} onChange={e => setCreateForm({...createForm, joueurId: e.target.value})} className="input-field" required>
                  <option value="">Choisir un joueur...</option>
                  {(Array.isArray(players) ? players : []).map(p => (
                    <option key={p.id} value={p.id}>{p.prenom} {p.nom}</option>
                  ))}
                </select>
              </div>
              {selectedPlayer && (
                <div className="p-3 rounded-lg text-sm" style={{ background: '#F0EEE4' }}>
                  <div>Parent: <strong>{selectedPlayer.parentPrenom} {selectedPlayer.parentNom}</strong></div>
                  <div>Fréquence actuelle: <strong>{FREQ_LABELS[selectedPlayer.frequence]}</strong></div>
                </div>
              )}
              <div>
                <label className="label">Fréquence de paiement</label>
                <select value={createForm.frequence} onChange={e => {
                  const freq = e.target.value;
                  setCreateForm({...createForm, frequence: freq, montant: FREQ_AMOUNTS[freq] || 60});
                }} className="input-field">
                  <option value="MENSUEL">Mensuel — 60 TND</option>
                  <option value="TRIMESTRIEL">Trimestriel — 180 TND</option>
                  <option value="SEMESTRIEL">Semestriel — 360 TND</option>
                  <option value="ANNUEL">Annuel — 720 TND</option>
                </select>
              </div>
              <div>
                <label className="label">Mois concerné</label>
                <select value={createForm.mois} onChange={e => setCreateForm({...createForm, mois: e.target.value})} className="input-field">
                  {MONTHS_FR.map(m => (
                    <option key={m.key} value={m.key}>{m.label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Montant payé (TND)</label>
                <input type="number" min="0" step="0.001" value={createForm.montant} onChange={e => setCreateForm({...createForm, montant: parseFloat(e.target.value) || 0})} className="input-field" />
              </div>
              <div>
                <label className="label">Moyen de paiement</label>
                <select value={createForm.moyenPaiement} onChange={e => setCreateForm({...createForm, moyenPaiement: e.target.value})} className="input-field">
                  <option value="ESPECES">Espèces</option>
                  <option value="VIREMENT" disabled>Virement (bientôt)</option>
                  <option value="CARTE_BANCAIRE" disabled>Carte bancaire (bientôt)</option>
                  <option value="EN_LIGNE" disabled>En ligne (bientôt)</option>
                </select>
              </div>
              <div>
                <label className="label">Statut</label>
                <select value={createForm.statut} onChange={e => setCreateForm({...createForm, statut: e.target.value})} className="input-field">
                  <option value="EN_ATTENTE">En attente</option>
                  <option value="PAYE">Payé</option>
                </select>
              </div>
              <div>
                <label className="label">Commentaire</label>
                <input value={createForm.commentaire} onChange={e => setCreateForm({...createForm, commentaire: e.target.value})} className="input-field" placeholder="Optionnel" />
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setShowCreateModal(false)} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => {
                  const [year, month] = createForm.mois.split('-');
                  createMutation.mutate({
                    joueurId: createForm.joueurId,
                    parentId: selectedPlayer?.parentId,
                    montant: createForm.montant,
                    formule: createForm.frequence,
                    dateEcheance: `${year}-${month}-01`,
                    moyenPaiement: createForm.moyenPaiement,
                    statut: createForm.statut,
                    commentaire: createForm.commentaire,
                  });
                }}
                disabled={!createForm.joueurId || !createForm.montant || createMutation.isPending}
                className="btn-primary text-sm"
              >
                {createMutation.isPending ? 'Enregistrement...' : 'Enregistrer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Pay Modal */}
      {showPayModal && payTarget && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[420px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Marquer payé</h3>
              <button onClick={() => { setShowPayModal(false); setPayTarget(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div className="p-3 rounded-lg text-sm" style={{ background: '#F0EEE4' }}>
                <div><strong>{payTarget.joueurPrenom} {payTarget.joueurNom}</strong></div>
                <div style={{ color: 'var(--ink-soft)' }}>Échéance: {payTarget.dateEcheance} · {FREQ_LABELS[payTarget.formule] || FREQ_LABELS[payTarget.frequence] || '—'}</div>
              </div>
              <div>
                <label className="label">Montant payé (TND)</label>
                <input type="number" min="0" step="0.001" value={payForm.montant} onChange={e => setPayForm({...payForm, montant: parseFloat(e.target.value) || 0})} className="input-field" />
              </div>
              <div>
                <label className="label">Date de paiement</label>
                <input type="date" value={payForm.datePaiement} onChange={e => setPayForm({...payForm, datePaiement: e.target.value})} className="input-field" />
              </div>
              <div>
                <label className="label">Moyen de paiement</label>
                <select value={payForm.moyenPaiement} onChange={e => setPayForm({...payForm, moyenPaiement: e.target.value})} className="input-field">
                  <option value="ESPECES">Espèces</option>
                  <option value="VIREMENT">Virement</option>
                  <option value="CARTE_BANCAIRE">Carte bancaire</option>
                  <option value="EN_LIGNE">En ligne</option>
                </select>
              </div>
              <div>
                <label className="label">Commentaire</label>
                <input value={payForm.commentaire} onChange={e => setPayForm({...payForm, commentaire: e.target.value})} className="input-field" placeholder="Optionnel" />
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowPayModal(false); setPayTarget(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => payMutation.mutate({
                  id: payTarget.id,
                  data: {
                    montant: payForm.montant,
                    datePaiement: payForm.datePaiement,
                    moyenPaiement: payForm.moyenPaiement,
                    commentaire: payForm.commentaire || null,
                  },
                })}
                disabled={!payForm.montant || payMutation.isPending}
                className="btn-primary text-sm"
              >
                {payMutation.isPending ? 'Enregistrement...' : 'Confirmer le paiement'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Export Modal */}
      {showExportModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Exporter les paiements</h3>
              <button onClick={() => setShowExportModal(false)} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div className="flex gap-3">
                <div className="flex-1">
                  <label className="label">Date début</label>
                  <input type="date" value={exportDates.start} onChange={e => setExportDates({...exportDates, start: e.target.value})} className="input-field" />
                </div>
                <div className="flex-1">
                  <label className="label">Date fin</label>
                  <input type="date" value={exportDates.end} onChange={e => setExportDates({...exportDates, end: e.target.value})} className="input-field" />
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => handleExport('csv')} className="btn-ghost text-sm">CSV</button>
              <button onClick={() => handleExport('excel')} className="btn-primary text-sm">Excel</button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && deleteTarget && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[400px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--red)' }}>Supprimer le paiement</h3>
              <button onClick={() => { setShowDeleteModal(false); setDeleteTarget(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
            </div>
            <div className="px-6 py-5">
              <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
                Voulez-vous vraiment supprimer le paiement de <strong>{deleteTarget.joueurPrenom} {deleteTarget.joueurNom}</strong> ?
              </p>
              <p className="text-sm mt-2 font-mono" style={{ color: 'var(--pitch-dark)' }}>
                {deleteTarget.montant?.toLocaleString('fr-TN')} DT — {deleteTarget.dateEcheance || '—'}
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowDeleteModal(false); setDeleteTarget(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={() => deleteMutation.mutate(deleteTarget.id)}
                disabled={deleteMutation.isPending}
                className="btn-primary text-sm"
                style={{ background: 'var(--red)' }}
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
