import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import api from '../api/client';
import { Building2, Users, GraduationCap, UserCog, BarChart3, TrendingUp, Power, MapPin, Calendar } from 'lucide-react';

export default function SuperAdminDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const { data } = await api.get('/stats/super-admin');
      setStats(data);
    } catch (err) {
      console.error('Failed to load super admin stats', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-500">Chargement...</div>;
  if (!stats) return <div className="p-8 text-center text-gray-500">Erreur de chargement</div>;

  const statCards = [
    { label: 'Total Académies', value: stats.totalAcademies, icon: Building2, color: 'bg-blue-500', bg: 'bg-blue-50' },
    { label: 'Actives', value: stats.activeAcademies, icon: Power, color: 'bg-green-500', bg: 'bg-green-50' },
    { label: 'Total Joueurs', value: stats.totalPlayers, icon: GraduationCap, color: 'bg-purple-500', bg: 'bg-purple-50' },
    { label: 'Total Parents', value: stats.totalParents, icon: UserCog, color: 'bg-orange-500', bg: 'bg-orange-50' },
    { label: 'Total Coachs', value: stats.totalCoaches, icon: Users, color: 'bg-teal-500', bg: 'bg-teal-50' },
    { label: 'Total Utilisateurs', value: stats.totalUsers, icon: BarChart3, color: 'bg-indigo-500', bg: 'bg-indigo-50' },
  ];

  const planColors = {
    FREE: 'bg-gray-100 text-gray-700',
    PRO: 'bg-blue-100 text-blue-700',
    PREMIUM: 'bg-purple-100 text-purple-700'
  };

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Super Admin Dashboard</h1>
        <p className="text-gray-500 mt-1">Vue d'ensemble de toutes les académies</p>
      </div>

      {/* Global Stats */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
        {statCards.map((card, i) => (
          <div key={i} className={`${card.bg} rounded-xl p-4`}>
            <div className="flex items-center gap-2 mb-2">
              <div className={`${card.color} rounded-lg p-1.5`}>
                <card.icon size={14} className="text-white" />
              </div>
              <span className="text-xs font-medium text-gray-600">{card.label}</span>
            </div>
            <p className="text-2xl font-bold text-gray-900">{card.value}</p>
          </div>
        ))}
      </div>

      {/* Plan Distribution */}
      {stats.byPlan && stats.byPlan.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Répartition par plan</h2>
          <div className="flex gap-4">
            {stats.byPlan.map((plan, i) => (
              <div key={i} className="flex items-center gap-3">
                <span className={`px-3 py-1.5 text-sm font-medium rounded-full ${planColors[plan.plan] || planColors.FREE}`}>
                  {plan.plan}
                </span>
                <span className="text-lg font-bold text-gray-900">{plan.count}</span>
                <span className="text-sm text-gray-500">académie{plan.count > 1 ? 's' : ''}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Academies Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-5 border-b border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900">Toutes les académies</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Académie</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Ville</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Plan</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Joueurs</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Parents</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Coachs</th>
                <th className="text-center px-5 py-3 text-xs font-semibold text-gray-500 uppercase">Statut</th>
              </tr>
            </thead>
            <tbody>
              {stats.academies.map(academy => (
                <tr key={academy.id} className="border-t border-gray-100 hover:bg-gray-50 transition">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${academy.active ? 'bg-green-100' : 'bg-red-100'}`}>
                        <Building2 size={16} className={academy.active ? 'text-green-600' : 'text-red-600'} />
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">{academy.nom}</p>
                        <p className="text-xs text-gray-500">{academy.slug}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <MapPin size={12} className="text-gray-400" />
                      {academy.ville || '—'}
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${planColors[academy.plan] || planColors.FREE}`}>
                      {academy.plan}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-center">
                    <span className="text-sm font-semibold text-gray-900">{academy.joueurCount}</span>
                  </td>
                  <td className="px-5 py-4 text-center">
                    <span className="text-sm font-semibold text-gray-900">{academy.parentCount}</span>
                  </td>
                  <td className="px-5 py-4 text-center">
                    <span className="text-sm font-semibold text-gray-900">{academy.coachCount}</span>
                  </td>
                  <td className="px-5 py-4 text-center">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${academy.active ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
                      {academy.active ? 'Actif' : 'Inactif'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {stats.academies.length === 0 && (
            <div className="text-center py-12 text-gray-400">
              <Building2 size={48} className="mx-auto mb-4 opacity-50" />
              <p>Aucune académie enregistrée</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
