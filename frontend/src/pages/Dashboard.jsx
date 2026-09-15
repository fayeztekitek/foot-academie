import { useQuery } from '@tanstack/react-query';
import { dashboardApi, statsApi, slotsApi, presencesApi } from '../api';
import BarChart from '../components/BarChart';
import { useAuth } from '../hooks/useAuth';
import { Calendar, BarChart3, TrendingUp, Clock, AlertTriangle, CheckCircle, Users, CreditCard } from 'lucide-react';

const DAY_MAP = {
  0: 'DIMANCHE', 1: 'LUNDI', 2: 'MARDI', 3: 'MERCREDI',
  4: 'JEUDI', 5: 'VENDREDI', 6: 'SAMEDI',
};

function formatTime(t) {
  if (!t) return '—';
  const parts = t.split(':');
  return `${parts[0]}:${parts[1]}`;
}

function formatDuration(start, end) {
  if (!start || !end) return '—';
  const [sh, sm] = start.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  const mins = (eh * 60 + em) - (sh * 60 + sm);
  if (mins <= 0) return '—';
  return `${mins} min`;
}

function AttendanceCard({ label, stats, icon: Icon }) {
  const rate = stats?.attendanceRate || 0;
  const color = rate >= 80 ? 'var(--grass)' : rate >= 60 ? 'var(--gold)' : 'var(--red)';
  return (
    <div className="bg-white p-4 sm:p-5">
      <div className="flex items-center gap-2 mb-1.5">
        {Icon && <Icon size={16} style={{ color: 'var(--ink-muted)' }} />}
        <div className="text-[11px] font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>{label}</div>
      </div>
      <div className="font-bebas text-[34px] sm:text-[38px] leading-none" style={{ color }}>
        {rate.toFixed(1)}%
      </div>
      <div className="text-xs mt-1" style={{ color: 'var(--ink-soft)' }}>
        {stats?.presentCount || 0}/{stats?.totalSessions || 0} séances
      </div>
    </div>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const today = DAY_MAP[new Date().getDay()];

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => dashboardApi.getStats().then(r => r.data),
  });

  const { data: detailed } = useQuery({
    queryKey: ['detailed-stats'],
    queryFn: () => statsApi.getDetailed().then(r => r.data),
  });

  const { data: todaySlots, isLoading: slotsLoading } = useQuery({
    queryKey: ['today-slots', today],
    queryFn: () => slotsApi.getAll({ jour: today }).then(r => r.data),
  });

  const { data: attendance, isLoading: attLoading } = useQuery({
    queryKey: ['attendance-global'],
    queryFn: () => presencesApi.getGlobalStats().then(r => r.data),
    enabled: user?.role === 'ADMIN',
  });

  const { data: childrenStats } = useQuery({
    queryKey: ['attendance-children'],
    queryFn: () => presencesApi.getMyChildrenStats().then(r => r.data),
    enabled: user?.role === 'PARENT',
  });

  if (isLoading) return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[1,2,3,4].map(i => <div key={i} className="skeleton h-28 rounded-xl" />)}
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="skeleton h-48 rounded-xl" />
        <div className="skeleton h-48 rounded-xl" />
      </div>
    </div>
  );

  if (user?.role === 'PARENT') {
    const kids = Array.isArray(childrenStats) ? childrenStats : [];
    return (
      <div>
        <div className="mb-6">
          <h2 className="font-bebas text-2xl" style={{ color: 'var(--pitch-dark)' }}>Mes enfants</h2>
          <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
            Suivi de la présence et des paiements de vos enfants.
          </p>
        </div>

        {kids.length === 0 ? (
          <div className="panel p-6 text-center" style={{ color: 'var(--ink-soft)' }}>Aucune donnée de présence disponible</div>
        ) : kids.map(child => (
          <div key={child.joueurId} className="panel rounded-xl border border-line shadow-card mb-5">
            <div className="px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
              <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>
                {child.joueurPrenom} {child.joueurNom}
              </h3>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-5 gap-px" style={{ background: 'var(--line)' }}>
              <AttendanceCard label="Mois" stats={child.mois} icon={Calendar} />
              <AttendanceCard label="Trimestre" stats={child.trimestre} icon={BarChart3} />
              <AttendanceCard label="Semestre" stats={child.semestre} icon={TrendingUp} />
              <AttendanceCard label="Année" stats={child.annee} icon={Clock} />
              <AttendanceCard label="Tout temps" stats={child.allTime} icon={Users} />
            </div>
          </div>
        ))}
      </div>
    );
  }

  const globalAtt = attendance?.mois || {};
  const statRow = [
    { label: 'Joueurs inscrits', value: stats?.totalPlayers || 0, delta: `${stats?.activePlayers || 0} actifs`, up: true },
    { label: 'Présence ce mois', value: `${(globalAtt.attendanceRate || 0).toFixed(1)}%`, delta: `${globalAtt.presentCount || 0}/${globalAtt.totalSessions || 0} séances`, up: (globalAtt.attendanceRate || 0) >= 70 },
    { label: 'Paiements en retard', value: stats?.pendingPayments || 0, delta: `${stats?.complianceAlerts || 0} alertes`, up: false },
    { label: 'Revenu du mois', value: `${Number(stats?.totalRevenue || 0).toLocaleString('fr-TN')}`, suffix: 'DT', delta: '+8% vs mois dernier', up: true },
  ];

  const slots = Array.isArray(todaySlots) ? todaySlots : todaySlots?.content || [];
  const alerts = [];
  if ((stats?.joueursSansCertificatMedical || 0) > 0) {
    alerts.push({ dot: 'red', title: `${stats.joueursSansCertificatMedical} certificat(s) médical(aux) manquant(s)`, sub: 'Documents obligatoires — à faire vérifier par les parents' });
  }
  if ((stats?.joueursSansAutorisationParentale || 0) > 0) {
    alerts.push({ dot: 'red', title: `${stats.joueursSansAutorisationParentale} autorisation(s) parentale(s) manquante(s)`, sub: 'Obligatoire pour les déplacements et tournois' });
  }
  if ((stats?.expiredDocuments || 0) > 0) {
    alerts.push({ dot: 'red', title: `${stats.expiredDocuments} document(s) expiré(s)`, sub: 'Obligatoire pour la licence FTF — à régulariser' });
  }
  if ((stats?.expiringDocuments || 0) > 0) {
    alerts.push({ dot: 'gold', title: `${stats.expiringDocuments} document(s) expirant dans 30 jours`, sub: 'Renouvellement licence FTF ou certificats médicaux' });
  }
  if ((stats?.parentsImpayes2Mois || 0) > 0) {
    alerts.push({ dot: 'red', title: `${stats.parentsImpayes2Mois} parent(s) en retard de paiement (2+ mois)`, sub: 'Régularisation de situation requise pour les enfants concernés' });
  }
  if (alerts.length === 0) {
    alerts.push({ dot: 'green', title: 'Tout est en ordre', sub: 'Aucune alerte de conformité en cours' });
  }

  const categoryData = detailed?.playersByCategory
    ? Object.entries(detailed.playersByCategory).map(([label, value]) => ({ label, value }))
    : [];
  const revenueData = detailed?.revenueHistory?.map(r => ({ label: r.month, value: Number(r.revenue) || 0 })) || [];

  return (
    <div>
      {/* Stat row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-6">
        {statRow.map((s, i) => (
          <div key={i} className="bg-white p-4 sm:p-5 rounded-xl border border-line shadow-card hover:shadow-card-hover transition-shadow duration-200">
            <div className="text-[11px] font-semibold uppercase tracking-wider" style={{ color: 'var(--ink-soft)' }}>{s.label}</div>
            <div className="font-bebas text-[34px] sm:text-[38px] mt-1 leading-none" style={{ color: 'var(--pitch-dark)' }}>
              {s.value}{s.suffix && <span className="text-base font-worksans"> {s.suffix}</span>}
            </div>
            <div className={`text-xs mt-1 font-medium ${s.up ? 'text-grass' : 'text-red'}`}>{s.delta}</div>
          </div>
        ))}
      </div>

      {/* Attendance by period */}
      {attendance && (
        <div className="mb-6">
          <h3 className="font-bebas text-lg mb-3" style={{ color: 'var(--pitch-dark)' }}>Présence par période</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
            <AttendanceCard label="Ce mois" stats={attendance.mois} icon={Calendar} />
            <AttendanceCard label="Ce trimestre" stats={attendance.trimestre} icon={BarChart3} />
            <AttendanceCard label="Ce semestre" stats={attendance.semestre} icon={TrendingUp} />
            <AttendanceCard label="Cette année" stats={attendance.annee} icon={Clock} />
          </div>
        </div>
      )}

      {/* Two-column grid */}
      <div className="grid grid-cols-1 lg:grid-cols-[1.5fr_1fr] gap-4 sm:gap-5 items-start">
        {/* Training panel — real data */}
        <div className="panel rounded-xl border border-line shadow-card">
          <div className="flex justify-between items-center px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
            <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>
              Entraînements du jour — {today.charAt(0) + today.slice(1).toLowerCase()}
            </h3>
            <a href="/training" className="text-xs font-semibold no-underline" style={{ color: 'var(--pitch)' }}>Voir le planning →</a>
          </div>
          <div className="py-1">
            {slotsLoading ? (
              <div className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Chargement des créneaux...</div>
            ) : slots.length === 0 ? (
              <div className="py-6 text-center" style={{ color: 'var(--ink-soft)' }}>Aucun entraînement prévu aujourd'hui ({today.charAt(0) + today.slice(1).toLowerCase()})</div>
            ) : slots.map((s, i) => (
              <div key={s.id || i} className="flex gap-3.5 items-center px-5 py-3.5 border-b last:border-b-0 hover:bg-gray-50/50 transition-colors" style={{ borderColor: '#F0EEE4' }}>
                <div className="w-14 shrink-0 text-center">
                  <div className="font-bebas text-xl" style={{ color: 'var(--pitch-dark)' }}>{formatTime(s.heureDebut)}</div>
                  <div className="text-[10.5px]" style={{ color: 'var(--ink-soft)' }}>{formatDuration(s.heureDebut, s.heureFin)}</div>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="text-sm font-semibold">{s.categorieNom || '—'}</div>
                  <div className="text-xs truncate" style={{ color: 'var(--ink-soft)' }}>
                    Coach {s.entraineurPrenom ? `${s.entraineurPrenom} ${s.entraineurNom}` : '—'} · {s.terrain || '—'}
                  </div>
                </div>
                <span className={`pill text-[11px] font-semibold px-2.5 py-1 rounded-full shrink-0 ${s.terrain?.toLowerCase().includes('salle') ? 'gold-light text-[#8A6A15]' : 'grass-light text-grass'}`}>
                  {s.terrain?.toLowerCase().includes('salle') ? 'Salle' : 'Terrain'}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Alerts panel — real data */}
        <div className="panel rounded-xl border border-line shadow-card">
          <div className="flex justify-between items-center px-5 py-4 border-b" style={{ borderColor: 'var(--line)' }}>
            <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Alertes conformité</h3>
          </div>
          <div className="py-1">
            {alerts.map((a, i) => (
              <div key={i} className="flex gap-3 px-5 py-3.5 border-b last:border-b-0 items-start" style={{ borderColor: '#F0EEE4' }}>
                {a.dot === 'red' ? <AlertTriangle size={16} className="mt-0.5 shrink-0 text-red" /> :
                 a.dot === 'gold' ? <AlertTriangle size={16} className="mt-0.5 shrink-0 text-gold" /> :
                 <CheckCircle size={16} className="mt-0.5 shrink-0 text-grass" />}
                <div className="min-w-0">
                  <div className="text-[13.5px] font-semibold">{a.title}</div>
                  <div className="text-xs mt-0.5" style={{ color: 'var(--ink-soft)' }}>{a.sub}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-5 mt-5 sm:mt-6">
        {categoryData.length > 0 && (
          <div className="panel rounded-xl border border-line shadow-card p-5">
            <h3 className="font-bebas text-lg mb-4" style={{ color: 'var(--pitch-dark)' }}>Joueurs par catégorie</h3>
            <BarChart data={categoryData} height={160} />
          </div>
        )}
        {revenueData.length > 0 && (
          <div className="panel rounded-xl border border-line shadow-card p-5">
            <h3 className="font-bebas text-lg mb-4" style={{ color: 'var(--pitch-dark)' }}>Revenus (6 mois)</h3>
            <BarChart data={revenueData} height={160} />
          </div>
        )}
      </div>
    </div>
  );
}
