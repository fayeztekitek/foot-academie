import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentsApi, playersApi } from '../api';
import { useAuth } from '../hooks/useAuth';
import { CreditCard, Calendar, AlertTriangle, CheckCircle, Clock } from 'lucide-react';

const STATUT_LABELS = {
  EN_ATTENTE: 'En attente',
  PAYE: 'Payé',
  EN_RETARD: 'En retard',
  ANNULE: 'Annulé',
};
const MOYEN_LABELS = {
  ESPECES: 'Espèces',
  VIREMENT: 'Virement',
  CARTE_BANCAIRE: 'Carte bancaire',
  EN_LIGNE: 'En ligne',
};

function StatusIcon({ statut }) {
  if (statut === 'PAYE') return <CheckCircle size={18} className="text-grass" />;
  if (statut === 'EN_RETARD') return <AlertTriangle size={18} className="text-red" />;
  return <Clock size={18} className="text-gold" />;
}

export default function ParentPayments() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [updateForm, setUpdateForm] = useState({ moyenPaiement: 'ESPECES', commentaire: '' });

  const { data: myPayments, isLoading } = useQuery({
    queryKey: ['my-payments'],
    queryFn: () => paymentsApi.getAll({ page: 0, size: 100 }).then(r => r.data?.content || r.data),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => paymentsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['my-payments']);
      setShowUpdateModal(false);
      setSelectedPayment(null);
      setUpdateForm({ moyenPaiement: 'ESPECES', commentaire: '' });
    },
  });

  const payments = Array.isArray(myPayments) ? myPayments : [];

  const totalDue = payments.filter(p => p.statut !== 'PAYE' && p.statut !== 'ANNULE').reduce((s, p) => s + (p.montant || 0), 0);
  const totalPaid = payments.filter(p => p.statut === 'PAYE').reduce((s, p) => s + (p.montant || 0), 0);
  const overdueCount = payments.filter(p => p.statut === 'EN_RETARD').length;

  const handleUpdatePayment = () => {
    if (!selectedPayment) return;
    updateMutation.mutate({
      id: selectedPayment.id,
      data: {
        moyenPaiement: updateForm.moyenPaiement,
        commentaire: updateForm.commentaire,
      },
    });
  };

  if (isLoading) return <div className="flex items-center justify-center h-64">Chargement...</div>;

  return (
    <div className="max-w-3xl mx-auto">
      {/* Header */}
      <div className="mb-6">
        <h2 className="font-bebas text-2xl" style={{ color: 'var(--pitch-dark)' }}>Mes paiements</h2>
        <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
          Consultez l'état de vos cotisations et mettez à jour vos moyens de paiement.
        </p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <div className="panel p-5">
          <div className="flex items-center gap-2 mb-2">
            <CreditCard size={16} style={{ color: 'var(--pitch)' }} />
            <div className="text-xs font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>Total dû</div>
          </div>
          <div className="font-mono text-2xl font-medium" style={{ color: totalDue > 0 ? 'var(--red)' : 'var(--grass)' }}>
            {totalDue.toLocaleString('fr-TN')} DT
          </div>
        </div>
        <div className="panel p-5">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle size={16} className="text-grass" />
            <div className="text-xs font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>Total payé</div>
          </div>
          <div className="font-mono text-2xl font-medium" style={{ color: 'var(--grass)' }}>
            {totalPaid.toLocaleString('fr-TN')} DT
          </div>
        </div>
        <div className="panel p-5">
          <div className="flex items-center gap-2 mb-2">
            <AlertTriangle size={16} className="text-red" />
            <div className="text-xs font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>En retard</div>
          </div>
          <div className="font-mono text-2xl font-medium" style={{ color: overdueCount > 0 ? 'var(--red)' : 'var(--ink-soft)' }}>
            {overdueCount}
          </div>
        </div>
      </div>

      {/* Payments list */}
      <div className="panel">
        <div className="px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
          <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Historique des paiements</h3>
        </div>
        <div className="divide-y" style={{ borderColor: '#F0EEE4' }}>
          {payments.length === 0 ? (
            <div className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun paiement enregistré</div>
          ) : payments.map(p => (
            <div key={p.id} className="flex items-center gap-4 px-5 py-4 border-b" style={{ borderColor: '#F0EEE4' }}>
              <StatusIcon statut={p.statut} />
              <div className="flex-1">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="text-sm font-semibold">
                      {p.joueurPrenom} {p.joueurNom}
                    </div>
                    <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>
                      {MOYEN_LABELS[p.moyenPaiement] || 'Non défini'} · {p.formulePaiement || '—'}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-mono font-medium">{(p.montant || 0).toLocaleString('fr-TN')} DT</div>
                    <div className="text-[11px]" style={{ color: 'var(--ink-soft)' }}>
                      {p.dateEcheance || '—'}
                    </div>
                  </div>
                </div>
                <div className="flex justify-between items-center mt-2">
                  <span className={`pill ${
                    p.statut === 'PAYE' ? 'pill-ok' :
                    p.statut === 'EN_RETARD' ? 'pill-retard' :
                    'pill-attente'
                  }`}>
                    {STATUT_LABELS[p.statut] || p.statut}
                  </span>
                  {p.statut !== 'PAYE' && p.statut !== 'ANNULE' && (
                    <button
                      onClick={() => { setSelectedPayment(p); setShowUpdateModal(true); }}
                      className="text-xs font-medium cursor-pointer px-3 py-1.5 rounded-md"
                      style={{ color: '#fff', background: 'var(--red)' }}
                    >
                      Mettre à jour
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Update Payment Modal */}
      {showUpdateModal && selectedPayment && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[420px] rounded-[10px] overflow-hidden">
            <div className="flex justify-between items-center px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Mettre à jour le paiement</h3>
              <button onClick={() => { setShowUpdateModal(false); setSelectedPayment(null); }} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}>×</button>
            </div>
            <div className="px-6 py-5 flex flex-col gap-3.5">
              <div className="p-3 rounded-lg text-sm" style={{ background: '#F0EEE4' }}>
                <div>Joueur: <strong>{selectedPayment.joueurPrenom} {selectedPayment.joueurNom}</strong></div>
                <div>Montant: <strong className="font-mono">{(selectedPayment.montant || 0).toLocaleString('fr-TN')} DT</strong></div>
                <div>Échéance: <strong className="font-mono">{selectedPayment.dateEcheance || '—'}</strong></div>
              </div>
              <div>
                <label className="label">Moyen de paiement</label>
                <select value={updateForm.moyenPaiement} onChange={e => setUpdateForm({...updateForm, moyenPaiement: e.target.value})} className="input-field">
                  <option value="ESPECES">Espèces</option>
                  <option value="VIREMENT">Virement</option>
                  <option value="CARTE_BANCAIRE">Carte bancaire</option>
                  <option value="EN_LIGNE">En ligne</option>
                </select>
              </div>
              <div>
                <label className="label">Commentaire</label>
                <input value={updateForm.commentaire} onChange={e => setUpdateForm({...updateForm, commentaire: e.target.value})} className="input-field" placeholder="Optionnel — numéro de reçu, référence..." />
              </div>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => { setShowUpdateModal(false); setSelectedPayment(null); }} className="btn-ghost text-sm">Annuler</button>
              <button
                onClick={handleUpdatePayment}
                disabled={updateMutation.isPending}
                className="btn-primary text-sm"
              >
                {updateMutation.isPending ? 'Mise à jour...' : 'Enregistrer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
