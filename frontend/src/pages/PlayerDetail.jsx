import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { playersApi, notesApi, slotsApi, coachesApi } from '../api';
import { PhotoAvatar } from '../components/PhotoUpload';
import { ArrowLeft, Star, TrendingUp, Activity, Zap, Plus, X } from 'lucide-react';

const POSTES = {
  GARDIEN: 'Gardien',
  DEFENSEUR_CENTRAL: 'Déf. central',
  DEFENSEUR_LATERAL: 'Déf. latéral',
  MILIEU_DEFENSIF: 'Milieu déf.',
  MILIEU_OFFENSIF: 'Milieu off.',
  AILIER: 'Ailier',
  ATTAQUANT: 'Attaquant',
  BUTOIR: 'Butoir',
};

function RatingBar({ label, value, icon: Icon, color }) {
  const pct = value ? (value / 10) * 100 : 0;
  return (
    <div className="flex flex-col gap-1">
      <div className="flex items-center justify-between text-xs">
        <span className="flex items-center gap-1.5 font-medium" style={{ color: 'var(--ink-soft)' }}>
          <Icon size={13} /> {label}
        </span>
        <span className="font-bold text-sm" style={{ color }}>{value?.toFixed(1) || '—'}/10</span>
      </div>
      <div className="w-full h-2 rounded-full" style={{ background: 'var(--line)' }}>
        <div className="h-full rounded-full transition-all" style={{ width: `${pct}%`, background: color }} />
      </div>
    </div>
  );
}

function AddNoteModal({ joueurId, creneaux, onClose }) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ creneauId: '', entraineurId: '', physique: '', technique: '', explosivite: '', tactique: '', mental: '', endurance: '' });

  const { data: coaches } = useQuery({
    queryKey: ['coaches-list'],
    queryFn: () => coachesApi.getAll({ size: 50 }).then(r => r.data?.content || r.data),
  });

  const createNote = useMutation({
    mutationFn: (data) => notesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['notes', joueurId]);
      queryClient.invalidateQueries(['player-stats', joueurId]);
      onClose();
    },
  });

  const handleSubmit = () => {
    if (!form.creneauId || !form.entraineurId || !form.physique || !form.technique || !form.explosivite || !form.tactique || !form.mental || !form.endurance) return;
    createNote.mutate({
      joueurId: parseInt(joueurId),
      creneauId: parseInt(form.creneauId),
      entraineurId: parseInt(form.entraineurId),
      physique: parseFloat(form.physique),
      technique: parseFloat(form.technique),
      explosivite: parseFloat(form.explosivite),
      tactique: parseFloat(form.tactique),
      mental: parseFloat(form.mental),
      endurance: parseFloat(form.endurance),
    });
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 p-5" style={{ background: 'rgba(18,32,26,.55)' }}>
      <div className="bg-white w-full max-w-[460px] rounded-[10px] overflow-hidden max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center px-6 py-4 border-b sticky top-0 bg-white" style={{ borderColor: 'var(--line)' }}>
          <h3 className="font-bebas text-xl m-0" style={{ color: 'var(--pitch-dark)' }}>Ajouter une note complète</h3>
          <button onClick={onClose} className="cursor-pointer p-1" style={{ color: 'var(--ink-soft)' }}><X size={20} /></button>
        </div>
        <div className="px-6 py-5 flex flex-col gap-3.5">
          <div>
            <label className="label">Séance d'entraînement</label>
            <select value={form.creneauId} onChange={e => setForm({...form, creneauId: e.target.value})} className="input-field">
              <option value="">Choisir...</option>
              {creneaux?.map(c => (
                <option key={c.id} value={c.id}>{c.jourSemaine} {c.heureDebut}-{c.heureFin} ({c.categorieNom})</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Entraîneur</label>
            <select value={form.entraineurId} onChange={e => setForm({...form, entraineurId: e.target.value})} className="input-field">
              <option value="">Choisir...</option>
              {coaches?.map(c => (
                <option key={c.id} value={c.id}>{c.prenom} {c.nom}</option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="label">Physique /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.physique} onChange={e => setForm({...form, physique: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
            <div>
              <label className="label">Technique /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.technique} onChange={e => setForm({...form, technique: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
            <div>
              <label className="label">Explosiv. /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.explosivite} onChange={e => setForm({...form, explosivite: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
            <div>
              <label className="label">Tactique /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.tactique} onChange={e => setForm({...form, tactique: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
            <div>
              <label className="label">Mental /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.mental} onChange={e => setForm({...form, mental: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
            <div>
              <label className="label">Endurance /10</label>
              <input type="number" min="0" max="10" step="0.5" value={form.endurance} onChange={e => setForm({...form, endurance: e.target.value})} className="input-field" placeholder="0-10" />
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-2.5 px-6 py-3.5 border-t sticky bottom-0 bg-white" style={{ borderColor: 'var(--line)' }}>
          <button onClick={onClose} className="btn-ghost text-sm">Annuler</button>
          <button onClick={handleSubmit} disabled={createNote.isPending} className="btn-primary text-sm">
            {createNote.isPending ? 'Enregistrement...' : 'Enregistrer la note'}
          </button>
        </div>
      </div>
    </div>
  );
}

function RadarChart({ stats }) {
  const metrics = [
    { label: 'Physique', val: stats?.moyennePhysique || 5 },
    { label: 'Technique', val: stats?.moyenneTechnique || 5 },
    { label: 'Explosivité', val: stats?.moyenneExplosivite || 5 },
    { label: 'Tactique', val: stats?.moyenneGlobale ? stats.moyenneGlobale * 0.95 : 5 },
    { label: 'Mental', val: stats?.moyenneGlobale ? stats.moyenneGlobale * 1.05 : 5 },
    { label: 'Endurance', val: stats?.moyennePhysique || 5 },
  ];
  const size = 180;
  const center = size / 2;
  const radius = 60;
  const angleStep = (Math.PI * 2) / metrics.length;
  const points = metrics.map((m, i) => {
    const angle = i * angleStep - Math.PI / 2;
    const r = (Math.min(m.val, 10) / 10) * radius;
    return { x: center + r * Math.cos(angle), y: center + r * Math.sin(angle), ...m };
  });
  const polygonPoints = points.map(p => `${p.x},${p.y}`).join(' ');

  return (
    <div className="flex flex-col items-center justify-center p-2">
      <h3 className="font-bebas text-xs mb-1" style={{ color: 'var(--pitch-dark)' }}>Spider Chart Performance</h3>
      <svg width={size} height={size} className="overflow-visible">
        {[0.25, 0.5, 0.75, 1].map(factor => (
          <polygon
            key={factor}
            points={metrics.map((_, i) => {
              const angle = i * angleStep - Math.PI / 2;
              const r = radius * factor;
              return `${center + r * Math.cos(angle)},${center + r * Math.sin(angle)}`;
            }).join(' ')}
            fill="none"
            stroke="var(--line)"
            strokeWidth="1"
          />
        ))}
        {metrics.map((_, i) => {
          const angle = i * angleStep - Math.PI / 2;
          return <line key={i} x1={center} y1={center} x2={center + radius * Math.cos(angle)} y2={center + radius * Math.sin(angle)} stroke="var(--line)" strokeWidth="1" />;
        })}
        <polygon points={polygonPoints} fill="rgba(31,90,59,0.25)" stroke="var(--grass)" strokeWidth="2" />
        {points.map((p, i) => {
          const angle = i * angleStep - Math.PI / 2;
          const labelR = radius + 16;
          const lx = center + labelR * Math.cos(angle);
          const ly = center + labelR * Math.sin(angle);
          return (
            <g key={i}>
              <circle cx={p.x} cy={p.y} r="3" fill="var(--grass)" />
              <text x={lx} y={ly} fontSize="9" textAnchor="middle" dominantBaseline="middle" fill="var(--ink-soft)" fontWeight="600">
                {p.label}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

export default function PlayerDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [showAddNote, setShowAddNote] = useState(false);

  const { data: player, isLoading } = useQuery({
    queryKey: ['player', id],
    queryFn: () => playersApi.getById(id).then(r => r.data),
  });

  const { data: notesData } = useQuery({
    queryKey: ['notes', id],
    queryFn: () => notesApi.getByJoueur(id, { size: 50 }).then(r => r.data),
  });

  const { data: stats } = useQuery({
    queryKey: ['player-stats', id],
    queryFn: () => notesApi.getStats(id).then(r => r.data),
  });

  const { data: creneaux } = useQuery({
    queryKey: ['slots-list'],
    queryFn: () => slotsApi.getAll().then(r => r.data),
  });

  if (isLoading) return <div className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Chargement...</div>;
  if (!player) return <div className="py-8 text-center" style={{ color: 'var(--ink-soft)' }}>Joueur non trouvé</div>;

  const notes = notesData?.content || notesData || [];
  let postesSec = [];
  try {
    postesSec = player.postesSecondaires ? JSON.parse(player.postesSecondaires) : [];
  } catch {
    postesSec = [];
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/players')} className="icon-btn" style={{ color: 'var(--ink-soft)' }}>
          <ArrowLeft size={20} />
        </button>
        <h1 className="font-bebas text-2xl m-0" style={{ color: 'var(--pitch-dark)' }}>Détail du joueur</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div className="panel p-5 flex flex-col items-center gap-3">
          <PhotoAvatar photoUrl={player.photoUrl} prenom={player.prenom} nom={player.nom} size={96} />
          <div className="text-center">
            <div className="text-lg font-bold">{player.prenom} {player.nom}</div>
            <div className="text-sm" style={{ color: 'var(--ink-soft)' }}>Né(e) le {player.dateNaissance ? new Date(player.dateNaissance).toLocaleDateString('fr-FR') : '—'}</div>
          </div>
          {player.postePrincipal && (
            <span className="text-xs font-semibold px-3 py-1 rounded-full" style={{ background: 'var(--pitch-light)', color: 'var(--pitch-dark)' }}>
              {POSTES[player.postePrincipal] || player.postePrincipal}
            </span>
          )}
          {postesSec.length > 0 && (
            <div className="flex flex-wrap gap-1 justify-center">
              {postesSec.map(p => (
                <span key={p} className="text-[11px] px-2 py-0.5 rounded-full bg-gray-100" style={{ color: 'var(--ink-soft)' }}>
                  {POSTES[p] || p}
                </span>
              ))}
            </div>
          )}
          <div className="w-full grid grid-cols-2 gap-3 mt-2 text-sm">
            <div className="text-center p-2 rounded-lg" style={{ background: 'var(--bg-subtle)' }}>
              <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>Taille</div>
              <div className="font-bold">{player.taille ? `${player.taille} cm` : '—'}</div>
            </div>
            <div className="text-center p-2 rounded-lg" style={{ background: 'var(--bg-subtle)' }}>
              <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>Poids</div>
              <div className="font-bold">{player.poids ? `${player.poids} kg` : '—'}</div>
            </div>
          </div>
          <div className="w-full text-xs mt-1" style={{ color: 'var(--ink-soft)' }}>
            <div>Catégorie: <strong>{player.categorieNom || '—'}</strong></div>
            <div>Parent: <strong>{player.parentPrenom ? `${player.parentPrenom} ${player.parentNom}` : '—'}</strong></div>
          </div>
        </div>

        <div className="lg:col-span-2 flex flex-col gap-5">
          <div className="panel p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Statistiques</h2>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
              <div className="text-center p-3 rounded-lg" style={{ background: 'var(--bg-subtle)' }}>
                <div className="text-2xl font-bold" style={{ color: 'var(--pitch)' }}>{stats?.moyenneGlobale?.toFixed(1) || '—'}</div>
                <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>Note globale</div>
              </div>
              <div className="text-center p-3 rounded-lg" style={{ background: 'var(--bg-subtle)' }}>
                <div className="text-2xl font-bold" style={{ color: 'var(--grass)' }}>{stats?.moyenneMois?.toFixed(1) || '—'}</div>
                <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>Ce mois</div>
              </div>
              <div className="text-center p-3 rounded-lg" style={{ background: 'var(--bg-subtle)' }}>
                <div className="text-2xl font-bold" style={{ color: 'var(--gold)' }}>{stats?.totalNotes || 0}</div>
                <div className="text-xs" style={{ color: 'var(--ink-soft)' }}>Total notes</div>
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5 items-center">
              <div className="grid grid-cols-2 gap-2.5">
                <RatingBar label="Physique" value={stats?.moyennePhysique} icon={Activity} color="var(--grass)" />
                <RatingBar label="Technique" value={stats?.moyenneTechnique} icon={TrendingUp} color="var(--pitch)" />
                <RatingBar label="Explosiv." value={stats?.moyenneExplosivite} icon={Zap} color="var(--gold)" />
                <RatingBar label="Tactique" value={stats?.moyenneTactique} icon={Activity} color="#2B6CB0" />
                <RatingBar label="Mental" value={stats?.moyenneMental} icon={TrendingUp} color="#805AD5" />
                <RatingBar label="Endurance" value={stats?.moyenneEndurance} icon={Zap} color="#DD6B20" />
              </div>
              <div>
                <RadarChart stats={stats} />
              </div>
            </div>
          </div>

          <div className="panel p-5">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>Notes récentes</h2>
              <button onClick={() => setShowAddNote(true)} className="btn-primary text-xs flex items-center gap-1">
                <Plus size={14} /> Ajouter
              </button>
            </div>
            {notes.length === 0 ? (
              <div className="py-6 text-center text-sm" style={{ color: 'var(--ink-soft)' }}>Aucune note enregistrée</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[11px]">
                  <thead>
                    <tr>
                      {['Date', 'Entraîneur', 'Séance', 'Phys.', 'Tech.', 'Explo.', 'Tact.', 'Ment.', 'End.', 'Moy.'].map(h => (
                        <th key={h} className="text-left py-2 px-2 border-b font-semibold" style={{ color: 'var(--ink-soft)', borderColor: 'var(--line)' }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {notes.map(n => (
                      <tr key={n.id} className="border-b last:border-b-0" style={{ borderColor: 'var(--line)' }}>
                        <td className="py-2 px-2">{n.date ? new Date(n.date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' }) : '—'}</td>
                        <td className="py-2 px-2">{n.entraineurNom || '—'}</td>
                        <td className="py-2 px-2">{n.creneauDescription || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: 'var(--grass)' }}>{n.physique?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: 'var(--pitch)' }}>{n.technique?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: 'var(--gold)' }}>{n.explosivite?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: '#2B6CB0' }}>{n.tactique?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: '#805AD5' }}>{n.mental?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-semibold" style={{ color: '#DD6B20' }}>{n.endurance?.toFixed(1) || '—'}</td>
                        <td className="py-2 px-2 font-bold" style={{ color: 'var(--ink-dark)' }}>{n.noteGlobale?.toFixed(1) || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      {showAddNote && <AddNoteModal joueurId={id} creneaux={creneaux} onClose={() => setShowAddNote(false)} />}
    </div>
  );
}
