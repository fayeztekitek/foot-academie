import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import api from '../api/client';
import { Plus, Building2, Users, GraduationCap, Calendar, Edit, Trash2, Power, Search, X, AlertTriangle, CreditCard } from 'lucide-react';

export default function Academies() {
  const { isAdmin, isSuperAdmin } = useAuth();
  const [academies, setAcademies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [planFilter, setPlanFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingAcademy, setEditingAcademy] = useState(null);
  const [formData, setFormData] = useState({
    slug: '', nom: '', email: '', telephone: '', ville: '', adresse: '', plan: 'FREE'
  });
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => {
    loadAcademies();
  }, []);

  const loadAcademies = async () => {
    try {
      const { data } = await api.get('/academies');
      setAcademies(data.content || data);
    } catch (err) {
      console.error('Failed to load academies', err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = academies.filter(a => {
    if (search) {
      const q = search.toLowerCase();
      if (!a.nom?.toLowerCase().includes(q) && !a.ville?.toLowerCase().includes(q) && !a.slug?.toLowerCase().includes(q)) return false;
    }
    if (planFilter && a.plan !== planFilter) return false;
    if (activeFilter === 'active' && !a.active) return false;
    if (activeFilter === 'inactive' && a.active) return false;
    return true;
  });

  const handleOpenModal = (academy = null) => {
    if (academy) {
      setEditingAcademy(academy);
      setFormData({
        slug: academy.slug || '',
        nom: academy.nom || '',
        email: academy.email || '',
        telephone: academy.telephone || '',
        ville: academy.ville || '',
        adresse: academy.adresse || '',
        plan: academy.plan || 'FREE'
      });
    } else {
      setEditingAcademy(null);
      setFormData({ slug: '', nom: '', email: '', telephone: '', ville: '', adresse: '', plan: 'FREE' });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingAcademy) {
        await api.put(`/academies/${editingAcademy.id}`, formData);
      } else {
        await api.post('/academies', formData);
      }
      setShowModal(false);
      loadAcademies();
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de la sauvegarde');
    }
  };

  const handleDelete = async () => {
    if (!showDeleteConfirm) return;
    setDeleting(true);
    try {
      await api.delete(`/academies/${showDeleteConfirm.id}`);
      setShowDeleteConfirm(null);
      loadAcademies();
    } catch (err) {
      alert(err.response?.data?.message || 'Suppression impossible');
    } finally {
      setDeleting(false);
    }
  };

  const handleToggle = async (id) => {
    setActionLoading(id);
    try {
      await api.post(`/academies/${id}/toggle-active`);
      loadAcademies();
    } catch (err) {
      alert('Erreur lors du changement de statut');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeactivateIfUnpaid = async (id) => {
    setActionLoading(id);
    try {
      await api.post(`/academies/${id}/deactivate-if-unpaid`);
      loadAcademies();
    } catch (err) {
      alert('Erreur lors de la vérification d\'abonnement');
    } finally {
      setActionLoading(null);
    }
  };

  const planColors = {
    FREE: 'bg-gray-100 text-gray-700',
    PRO: 'bg-blue-100 text-blue-700',
    PREMIUM: 'bg-purple-100 text-purple-700'
  };

  if (loading) return <div className="p-8 text-center text-gray-500">Chargement...</div>;

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Académies</h1>
          <p className="text-gray-500 mt-1">Gestion des académies multi-tenants</p>
        </div>
        {isSuperAdmin && (
          <button
            onClick={() => handleOpenModal()}
            className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            <Plus size={18} />
            Nouvelle académie
          </button>
        )}
      </div>

      <div className="mb-4 relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
        <input
          type="text"
          placeholder="Rechercher par nom, ville, slug..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      <div className="flex gap-3 mb-4 flex-wrap items-center">
        <select
          value={planFilter}
          onChange={e => setPlanFilter(e.target.value)}
          className="input-field text-xs py-1.5 px-3"
        >
          <option value="">Tous les plans</option>
          <option value="FREE">Gratuit</option>
          <option value="PRO">Professionnel</option>
          <option value="PREMIUM">Premium</option>
        </select>
        <select
          value={activeFilter}
          onChange={e => setActiveFilter(e.target.value)}
          className="input-field text-xs py-1.5 px-3"
        >
          <option value="">Tous les statuts</option>
          <option value="active">Actif</option>
          <option value="inactive">Inactif</option>
        </select>
        {(planFilter || activeFilter) && (
          <button
            onClick={() => { setPlanFilter(''); setActiveFilter(''); }}
            className="text-xs flex items-center gap-1 px-2 py-1 rounded border cursor-pointer hover:bg-gray-50"
            style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}
          >
            Effacer filtres
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map(academy => (
          <div key={academy.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${academy.active ? 'bg-green-100' : 'bg-red-100'}`}>
                  <Building2 size={20} className={academy.active ? 'text-green-600' : 'text-red-600'} />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">{academy.nom}</h3>
                  <p className="text-sm text-gray-500">{academy.ville || '—'}</p>
                </div>
              </div>
              <span className={`px-2 py-1 text-xs font-medium rounded-full ${planColors[academy.plan] || planColors.FREE}`}>
                {academy.plan}
              </span>
            </div>

            <div className="grid grid-cols-3 gap-3 mb-4">
              <div className="text-center">
                <div className="flex items-center justify-center gap-1 text-gray-400 mb-1">
                  <GraduationCap size={14} />
                </div>
                <p className="text-lg font-bold text-gray-900">{academy.joueurCount || 0}</p>
                <p className="text-xs text-gray-500">Joueurs</p>
              </div>
              <div className="text-center">
                <div className="flex items-center justify-center gap-1 text-gray-400 mb-1">
                  <Users size={14} />
                </div>
                <p className="text-lg font-bold text-gray-900">{academy.parentCount || 0}</p>
                <p className="text-xs text-gray-500">Parents</p>
              </div>
              <div className="text-center">
                <div className="flex items-center justify-center gap-1 text-gray-400 mb-1">
                  <Calendar size={14} />
                </div>
                <p className="text-lg font-bold text-gray-900">{academy.coachCount || 0}</p>
                <p className="text-xs text-gray-500">Coachs</p>
              </div>
            </div>

            <div className="flex items-center justify-between pt-3 border-t border-gray-100">
              <span className={`px-2 py-1 text-xs rounded-full ${academy.active ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
                {academy.active ? 'Actif' : 'Inactif'}
              </span>
              {isSuperAdmin && (
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => handleToggle(academy.id)}
                    disabled={actionLoading === academy.id}
                    className="p-1.5 rounded-md hover:bg-gray-100 transition disabled:opacity-50"
                    title={academy.active ? 'Désactiver' : 'Activer'}
                  >
                    {actionLoading === academy.id ? (
                      <span className="w-3.5 h-3.5 border-2 border-gray-400 border-t-transparent rounded-full animate-spin inline-block" />
                    ) : (
                      <Power size={14} className={academy.active ? 'text-green-600' : 'text-red-600'} />
                    )}
                  </button>
                  <button
                    onClick={() => handleDeactivateIfUnpaid(academy.id)}
                    disabled={actionLoading === academy.id || !academy.active}
                    className="p-1.5 rounded-md hover:bg-orange-50 transition disabled:opacity-50"
                    title="Désactiver si impayé"
                  >
                    <CreditCard size={14} className="text-orange-600" />
                  </button>
                  <button
                    onClick={() => handleOpenModal(academy)}
                    className="p-1.5 rounded-md hover:bg-gray-100 transition"
                    title="Modifier"
                  >
                    <Edit size={14} className="text-gray-600" />
                  </button>
                  <button
                    onClick={() => setShowDeleteConfirm(academy)}
                    className="p-1.5 rounded-md hover:bg-red-50 transition"
                    title="Supprimer"
                  >
                    <Trash2 size={14} className="text-red-500" />
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="text-center py-12 text-gray-400">
          <Building2 size={48} className="mx-auto mb-4 opacity-50" />
          <p>Aucune académie trouvée</p>
        </div>
      )}

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 overflow-hidden max-h-[90vh] flex flex-col">
            <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h2 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>
                {editingAcademy ? 'Modifier l\'académie' : 'Nouvelle académie'}
              </h2>
              <button onClick={() => setShowModal(false)} className="text-xl cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="flex flex-col flex-1 overflow-hidden">
              <div className="px-6 py-5 space-y-4 overflow-y-auto flex-1">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Slug *</label>
                    <input
                      type="text"
                      value={formData.slug}
                      onChange={(e) => setFormData({...formData, slug: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                      required
                      disabled={!!editingAcademy}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                    <input
                      type="text"
                      value={formData.nom}
                      onChange={(e) => setFormData({...formData, nom: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                      required
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input
                      type="email"
                      value={formData.email}
                      onChange={(e) => setFormData({...formData, email: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
                    <input
                      type="text"
                      value={formData.telephone}
                      onChange={(e) => setFormData({...formData, telephone: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Ville</label>
                    <input
                      type="text"
                      value={formData.ville}
                      onChange={(e) => setFormData({...formData, ville: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Plan</label>
                    <select
                      value={formData.plan}
                      onChange={(e) => setFormData({...formData, plan: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="FREE">FREE</option>
                      <option value="PRO">PRO</option>
                      <option value="PREMIUM">PREMIUM</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
                  <input
                    type="text"
                    value={formData.adresse}
                    onChange={(e) => setFormData({...formData, adresse: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
                <button type="button" onClick={() => setShowModal(false)} className="btn-ghost text-sm">
                  Annuler
                </button>
                <button type="submit" className="btn-primary text-sm">
                  {editingAcademy ? 'Mettre à jour' : 'Créer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
          <div className="bg-white w-full max-w-[380px] rounded-[10px] overflow-hidden">
            <div className="px-6 py-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
                  <AlertTriangle size={20} className="text-red-600" />
                </div>
                <h3 className="font-bebas text-xl" style={{ color: 'var(--pitch-dark)' }}>Supprimer cette académie ?</h3>
              </div>
              <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
                Voulez-vous vraiment supprimer <strong>{showDeleteConfirm.nom}</strong> ? Toutes les données de cette académie (joueurs, coachs, parents, paiements, etc.) seront définitivement supprimées. Cette action est irréversible.
              </p>
            </div>
            <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t" style={{ borderColor: 'var(--line)' }}>
              <button onClick={() => setShowDeleteConfirm(null)} className="btn-ghost text-sm" disabled={deleting}>
                Annuler
              </button>
              <button onClick={handleDelete} disabled={deleting} className="btn-danger text-sm">
                {deleting ? 'Suppression...' : 'Supprimer'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
