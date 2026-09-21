import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import api from '../api/client';
import { CreditCard, Check, X, AlertTriangle, Users, GraduationCap, UserCog, FileText, ArrowRight, Search } from 'lucide-react';

const PLAN_DETAILS = {
  FREE: { label: 'Gratuit', color: 'bg-gray-100 text-gray-700', border: 'border-gray-300', icon: '⚽' },
  PRO: { label: 'Professionnel', color: 'bg-blue-100 text-blue-700', border: 'border-blue-400', icon: '🏆' },
  PREMIUM: { label: 'Premium', color: 'bg-purple-100 text-purple-700', border: 'border-purple-400', icon: '👑' },
};

export default function Billing() {
  const { isAdmin } = useAuth();
  const [abonnement, setAbonnement] = useState(null);
  const [factures, setFactures] = useState([]);
  const [plans, setPlans] = useState({});
  const [loading, setLoading] = useState(true);
  const [subscribing, setSubscribing] = useState(null);
  const [invoiceSearch, setInvoiceSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [abonnementRes, facturesRes, plansRes] = await Promise.all([
        api.get('/billing/abonnement'),
        api.get('/billing/factures'),
        api.get('/billing/plans')
      ]);
      setAbonnement(abonnementRes.data);
      setFactures(facturesRes.data);
      setPlans(plansRes.data?.plans || {});
    } catch (err) {
      console.error('Failed to load billing data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (plan) => {
    if (!confirm(`Souscrire au plan ${PLAN_DETAILS[plan]?.label || plan} ?`)) return;
    setSubscribing(plan);
    try {
      await api.post('/billing/subscribe', { plan });
      loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de la souscription');
    } finally {
      setSubscribing(null);
    }
  };

  const handleCancel = async () => {
    if (!confirm('Annuler votre abonnement ? Le service restera actif jusqu\'à la fin de la période.')) return;
    try {
      await api.post('/billing/cancel');
      loadData();
    } catch (err) {
      alert('Erreur lors de l\'annulation');
    }
  };

  const usagePercent = (used, max) => Math.min(100, Math.round((used / max) * 100));
  const usageColor = (pct) => pct >= 90 ? 'bg-red-500' : pct >= 70 ? 'bg-yellow-500' : 'bg-green-500';

  if (loading) return <div className="p-8 text-center text-gray-500">Chargement...</div>;

  const filteredFactures = factures.filter(f => {
    if (invoiceSearch) {
      const q = invoiceSearch.toLowerCase();
      if (!(f.numero || '').toLowerCase().includes(q) && !(f.description || '').toLowerCase().includes(q)) return false;
    }
    if (statusFilter && f.statut !== statusFilter) return false;
    return true;
  });

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Facturation & Abonnement</h1>
        <p className="text-gray-500 mt-1">Gérez votre plan et suivez vos factures</p>
      </div>

      {/* Current Plan */}
      {abonnement && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Plan actuel</h2>
            <span className={`px-3 py-1.5 text-sm font-medium rounded-full ${PLAN_DETAILS[abonnement.plan]?.color || 'bg-gray-100'}`}>
              {PLAN_DETAILS[abonnement.plan]?.icon} {PLAN_DETAILS[abonnement.plan]?.label || abonnement.plan}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <UsageCard
              icon={GraduationCap}
              label="Joueurs"
              used={abonnement.joueursUtilises}
              max={abonnement.maxJoueurs}
              limitReached={abonnement.joueursLimitReached}
              percent={usagePercent(abonnement.joueursUtilises, abonnement.maxJoueurs)}
              color={usageColor}
            />
            <UsageCard
              icon={UserCog}
              label="Entraîneurs"
              used={abonnement.coachsUtilises}
              max={abonnement.maxCoachs}
              limitReached={abonnement.coachsLimitReached}
              percent={usagePercent(abonnement.coachsUtilises, abonnement.maxCoachs)}
              color={usageColor}
            />
            <UsageCard
              icon={Users}
              label="Parents"
              used={abonnement.parentsUtilises}
              max={abonnement.maxParents}
              limitReached={abonnement.parentsLimitReached}
              percent={usagePercent(abonnement.parentsUtilises, abonnement.maxParents)}
              color={usageColor}
            />
          </div>

          <div className="flex items-center justify-between pt-4 border-t border-gray-100">
            <div className="text-sm text-gray-500">
              {abonnement.montantMensuel > 0 ? (
                <span>{abonnement.montantMensuel} {abonnement.devise}/mois</span>
              ) : (
                <span>Plan gratuit</span>
              )}
              {abonnement.dateFin && (
                <span className="ml-3">• Renouvellement le {new Date(abonnement.dateFin).toLocaleDateString('fr-FR')}</span>
              )}
            </div>
            {abonnement.plan !== 'FREE' && isAdmin && (
              <button
                onClick={handleCancel}
                className="text-sm text-red-600 hover:text-red-700 font-medium"
              >
                Annuler l'abonnement
              </button>
            )}
          </div>
        </div>
      )}

      {/* Plans */}
      {isAdmin && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Changer de plan</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Object.entries(plans).map(([key, plan]) => {
              const details = PLAN_DETAILS[key] || {};
              const isCurrent = abonnement?.plan === key;
              return (
                <div
                  key={key}
                  className={`bg-white rounded-xl border-2 p-6 transition hover:shadow-md ${
                    isCurrent ? 'border-blue-500 ring-2 ring-blue-200' : 'border-gray-200'
                  }`}
                >
                  <div className="text-center mb-4">
                    <span className="text-3xl">{details.icon}</span>
                    <h3 className="text-lg font-bold text-gray-900 mt-2">{details.label}</h3>
                    <p className="text-2xl font-bold text-gray-900 mt-1">{plan.prix}</p>
                  </div>
                  <ul className="space-y-2 mb-6">
                    <li className="flex items-center gap-2 text-sm text-gray-600">
                      <Check size={14} className="text-green-500" />
                      {plan.maxJoueurs} joueurs max
                    </li>
                    <li className="flex items-center gap-2 text-sm text-gray-600">
                      <Check size={14} className="text-green-500" />
                      {plan.maxCoachs} entraîneurs max
                    </li>
                    <li className="flex items-center gap-2 text-sm text-gray-600">
                      <Check size={14} className="text-green-500" />
                      {plan.maxParents} parents max
                    </li>
                  </ul>
                  <button
                    onClick={() => handleSubscribe(key)}
                    disabled={isCurrent || subscribing === key}
                    className={`w-full py-2.5 rounded-lg font-medium transition ${
                      isCurrent
                        ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                        : 'bg-blue-600 text-white hover:bg-blue-700'
                    }`}
                  >
                    {isCurrent ? 'Plan actuel' : subscribing === key ? 'Souscription...' : 'Choisir ce plan'}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Factures */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-5 border-b border-gray-100">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <h2 className="text-lg font-semibold text-gray-900">Historique des factures</h2>
            <div className="flex gap-3 items-center flex-wrap">
              <div className="relative">
                <Search size={14} className="absolute left-2.5 top-2.5" style={{ color: 'var(--ink-soft)' }} />
                <input
                  value={invoiceSearch}
                  onChange={e => setInvoiceSearch(e.target.value)}
                  placeholder="Rechercher numéro ou description…"
                  className="input-field pl-8 text-xs py-1.5"
                />
              </div>
              <select
                value={statusFilter}
                onChange={e => setStatusFilter(e.target.value)}
                className="input-field text-xs py-1.5 px-3"
              >
                <option value="">Tous les statuts</option>
                <option value="PAYEE">Payée</option>
                <option value="EN_ATTENTE">En attente</option>
                <option value="EN_RETARD">En retard</option>
              </select>
            </div>
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Numéro</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Description</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Montant</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Émise le</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Échéance</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Statut</th>
              </tr>
            </thead>
            <tbody>
              {filteredFactures.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-12 text-gray-400">
                    <FileText size={48} className="mx-auto mb-4 opacity-50" />
                    <p>{factures.length === 0 ? 'Aucune facture' : 'Aucune facture ne correspond aux filtres'}</p>
                  </td>
                </tr>
              ) : filteredFactures.map(f => (
                <tr key={f.id} className="border-t border-gray-100 hover:bg-gray-50 transition">
                  <td className="px-5 py-4 font-mono text-sm font-medium text-gray-900">{f.numero}</td>
                  <td className="px-5 py-4 text-sm text-gray-600">{f.description}</td>
                  <td className="px-5 py-4 text-sm font-semibold text-gray-900">{f.montant} {f.devise}</td>
                  <td className="px-5 py-4 text-sm text-gray-500">{new Date(f.dateEmission).toLocaleDateString('fr-FR')}</td>
                  <td className="px-5 py-4 text-sm text-gray-500">{new Date(f.dateEcheance).toLocaleDateString('fr-FR')}</td>
                  <td className="px-5 py-4 text-center">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                      f.statut === 'PAYEE' ? 'bg-green-50 text-green-700' :
                      f.statut === 'EN_ATTENTE' ? 'bg-yellow-50 text-yellow-700' :
                      f.statut === 'EN_RETARD' ? 'bg-red-50 text-red-700' :
                      'bg-gray-50 text-gray-700'
                    }`}>
                      {f.statut}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function UsageCard({ icon: Icon, label, used, max, limitReached, percent, color }) {
  return (
    <div className={`p-4 rounded-lg border ${limitReached ? 'border-red-200 bg-red-50' : 'border-gray-200 bg-gray-50'}`}>
      <div className="flex items-center gap-2 mb-2">
        <Icon size={16} className={limitReached ? 'text-red-500' : 'text-gray-500'} />
        <span className="text-sm font-medium text-gray-700">{label}</span>
        {limitReached && <AlertTriangle size={14} className="text-red-500 ml-auto" />}
      </div>
      <div className="flex items-end justify-between mb-2">
        <span className="text-2xl font-bold text-gray-900">{used}</span>
        <span className="text-sm text-gray-500">/ {max}</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div className={`${color(percent)} h-2 rounded-full transition-all`} style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}
