import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import api from '../api/client';
import { Plus, Mail, Clock, CheckCircle, XCircle, Trash2, Copy, UserPlus } from 'lucide-react';

export default function Invitations() {
  const { isAdmin } = useAuth();
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ email: '', role: 'COACH' });
  const [submitting, setSubmitting] = useState(false);
  const [copied, setCopied] = useState(null);

  useEffect(() => {
    loadInvitations();
  }, []);

  const loadInvitations = async () => {
    try {
      const { data } = await api.get('/invitations');
      setInvitations(data);
    } catch (err) {
      console.error('Failed to load invitations', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/invitations', form);
      setShowModal(false);
      setForm({ email: '', role: 'COACH' });
      loadInvitations();
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de la création');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (id) => {
    if (!confirm('Annuler cette invitation ?')) return;
    try {
      await api.post(`/invitations/${id}/cancel`);
      loadInvitations();
    } catch (err) {
      alert('Erreur lors de l\'annulation');
    }
  };

  const copyLink = (token) => {
    const link = `${window.location.origin}/invite/${token}`;
    navigator.clipboard.writeText(link);
    setCopied(token);
    setTimeout(() => setCopied(null), 2000);
  };

  const statusConfig = {
    EN_ATTENTE: { icon: Clock, color: 'text-yellow-500', bg: 'bg-yellow-50', label: 'En attente' },
    ACCEPTEE: { icon: CheckCircle, color: 'text-green-500', bg: 'bg-green-50', label: 'Acceptée' },
    EXPIREE: { icon: XCircle, color: 'text-red-500', bg: 'bg-red-50', label: 'Expirée' },
    ANNULEE: { icon: XCircle, color: 'text-gray-500', bg: 'bg-gray-50', label: 'Annulée' },
  };

  const roleLabels = {
    ADMIN: 'Administrateur',
    COACH: 'Entraîneur',
    PARENT: 'Parent'
  };

  if (loading) return <div className="p-8 text-center text-gray-500">Chargement...</div>;

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Invitations</h1>
          <p className="text-gray-500 mt-1">Inviter des membres par email</p>
        </div>
        {isAdmin && (
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            <Plus size={18} />
            Nouvelle invitation
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Email</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Rôle</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Statut</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Invité par</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Expire le</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody>
              {invitations.map(inv => {
                const status = statusConfig[inv.statut] || statusConfig.EN_ATTENTE;
                const StatusIcon = status.icon;
                return (
                  <tr key={inv.id} className="border-t border-gray-100 hover:bg-gray-50 transition">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2">
                        <Mail size={14} className="text-gray-400" />
                        <span className="font-medium text-gray-900">{inv.email}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <span className="px-2 py-1 text-xs font-medium rounded-full bg-blue-50 text-blue-700">
                        {roleLabels[inv.role] || inv.role}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className={`flex items-center gap-1.5 ${status.color}`}>
                        <StatusIcon size={14} />
                        <span className="text-sm font-medium">{status.label}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4 text-sm text-gray-500">{inv.invitedByEmail || '—'}</td>
                    <td className="px-5 py-4 text-sm text-gray-500">
                      {inv.dateExpiration ? new Date(inv.dateExpiration).toLocaleDateString('fr-FR') : '—'}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center justify-center gap-1">
                        {inv.statut === 'EN_ATTENTE' && (
                          <>
                            <button
                              onClick={() => copyLink(inv.token)}
                              className="p-1.5 rounded-md hover:bg-gray-100 transition"
                              title="Copier le lien"
                            >
                              {copied === inv.token ? (
                                <CheckCircle size={14} className="text-green-500" />
                              ) : (
                                <Copy size={14} className="text-gray-600" />
                              )}
                            </button>
                            <button
                              onClick={() => handleCancel(inv.id)}
                              className="p-1.5 rounded-md hover:bg-red-50 transition"
                              title="Annuler"
                            >
                              <Trash2 size={14} className="text-red-500" />
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          {invitations.length === 0 && (
            <div className="text-center py-12 text-gray-400">
              <UserPlus size={48} className="mx-auto mb-4 opacity-50" />
              <p>Aucune invitation</p>
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
            <h2 className="text-lg font-bold mb-4">Nouvelle invitation</h2>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({...form, email: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                  required
                  placeholder="membre@exemple.tn"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Rôle *</label>
                <select
                  value={form.role}
                  onChange={(e) => setForm({...form, role: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                >
                  <option value="ADMIN">Administrateur</option>
                  <option value="COACH">Entraîneur</option>
                  <option value="PARENT">Parent</option>
                </select>
              </div>
              <p className="text-xs text-gray-500">
                Un email sera envoyé avec un lien pour créer leur compte. Le lien expire après 7 jours.
              </p>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
                >
                  {submitting ? 'Envoi...' : 'Envoyer l\'invitation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
