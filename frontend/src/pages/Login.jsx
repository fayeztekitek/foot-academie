import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useDemo } from '../demo/DemoProvider';
import { LogIn, Smartphone, Settings, X, Building2, RefreshCw } from 'lucide-react';
import { authApi } from '../api/auth';

export default function Login() {
  const { login } = useAuth();
  const { isDemo, enableDemo, disableDemo, backendUrl, updateBackendUrl } = useDemo();
  const [email, setEmail] = useState(isDemo ? 'admin@nadi.tn' : '');
  const [motDePasse, setMotDePasse] = useState(isDemo ? 'admin123' : '');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [tempUrl, setTempUrl] = useState(backendUrl);

  const [tenants, setTenants] = useState([]);
  const [selectedTenantId, setSelectedTenantId] = useState(null);
  const [tenantsLoading, setTenantsLoading] = useState(true);
  const [tenantsError, setTenantsError] = useState(null);

  const fetchTenants = async () => {
    setTenantsLoading(true);
    setTenantsError(null);
    try {
      const { data } = await authApi.getTenants();
      setTenants(data);
      if (data.length === 1) {
        setSelectedTenantId(data[0].id);
      }
    } catch (err) {
      console.error('Failed to load tenants:', err);
      setTenantsError('Impossible de charger les académies. Vérifiez l\'URL du serveur.');
    } finally {
      setTenantsLoading(false);
    }
  };

  useEffect(() => {
    fetchTenants();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      disableDemo();
      await login(email, motDePasse, selectedTenantId);
    } catch (err) {
      setError(err.response?.data?.message || 'Identifiants incorrects');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async () => {
    enableDemo();
    setEmail('admin@nadi.tn');
    setMotDePasse('admin123');
    setError('');
    setLoading(true);
    try {
      setTimeout(async () => {
        try {
          await login('admin@nadi.tn', 'admin123');
        } catch {
          window.location.reload();
        }
      }, 100);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSettings = () => {
    updateBackendUrl(tempUrl);
    setShowSettings(false);
  };

  const demoRoles = [
    { role: 'ADMIN', label: 'Administrateur', desc: 'Accès complet à la gestion' },
    { role: 'COACH', label: 'Entraîneur', desc: 'Joueurs, entraînements' },
    { role: 'PARENT', label: 'Parent', desc: 'Paiements, infos enfant' },
  ];

  const [demoRole, setDemoRole] = useState('ADMIN');

  const handleDemoRoleLogin = async (role) => {
    enableDemo();
    const emails = { ADMIN: 'admin@nadi.tn', COACH: 'coach@nadi.tn', PARENT: 'parent@nadi.tn' };
    const passwords = { ADMIN: 'admin123', COACH: 'coach123', PARENT: 'parent123' };
    setLoading(true);
    try {
      await login(emails[role], passwords[role]);
    } catch {
      window.location.reload();
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-5" style={{ background: 'linear-gradient(135deg, #E6F1EA 0%, #dcfce7 100%)' }}>
      <div className="w-full max-w-md">
        {/* Brand header */}
        <div className="text-center mb-8">
          <div className="w-[60px] h-[60px] rounded-full mx-auto mb-4 flex items-center justify-center font-bebas text-2xl text-pitch-dark"
            style={{ background: 'conic-gradient(from 200deg, #2F6F4E, #C9A227, #2F6F4E)' }}>
            EF
          </div>
          <h1 className="font-bebas text-4xl mb-1" style={{ color: 'var(--pitch-dark)' }}>NADI</h1>
          <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>Académie de Football</p>
          {isDemo && (
            <div className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium"
              style={{ background: 'var(--gold-light, #FDF6E3)', color: 'var(--gold, #C9A227)' }}>
              <Smartphone size={12} />
              Mode Démo
            </div>
          )}
        </div>

        {/* Settings button */}
        <div className="text-right mb-3">
          <button
            type="button"
            onClick={() => setShowSettings(!showSettings)}
            className="text-xs flex items-center gap-1 ml-auto"
            style={{ color: 'var(--ink-soft)' }}
          >
            <Settings size={12} />
            Serveur API
          </button>
        </div>

        {/* Server settings panel */}
        {showSettings && (
          <div className="panel p-4 mb-4">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold" style={{ color: 'var(--pitch-dark)' }}>Configuration du serveur</h3>
              <button onClick={() => setShowSettings(false)} className="p-1 rounded hover:bg-gray-100">
                <X size={14} />
              </button>
            </div>
            <div className="mb-3">
              <label className="label">URL du backend (optionnel pour démo)</label>
              <input
                type="text"
                value={tempUrl}
                onChange={e => setTempUrl(e.target.value)}
                className="input-field"
                placeholder="http://192.168.1.100:8081/api"
              />
              <p className="text-[11px] mt-1" style={{ color: 'var(--ink-soft)' }}>
                Laissez vide pour la démo. Ex: http://10.0.2.2:8081/api (émulateur)
              </p>
            </div>
            <button onClick={handleSaveSettings} className="btn-primary w-full py-2 text-sm">
              Enregistrer
            </button>
          </div>
        )}

        {/* Login form */}
        <div className="panel p-6">
          <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>Connexion</h2>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            {/* Tenant selector */}
            <div>
              <label className="label">Académie</label>
              {tenantsError ? (
                <div className="flex flex-col gap-2">
                  <div className="text-xs px-3 py-2 rounded-lg" style={{ background: '#FBE7E7', color: 'var(--red)' }}>
                    {tenantsError}
                  </div>
                  <button
                    type="button"
                    onClick={fetchTenants}
                    className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-lg border self-start"
                    style={{ borderColor: 'var(--line)', color: 'var(--ink-soft)' }}
                  >
                    <RefreshCw size={12} /> Réessayer
                  </button>
                </div>
              ) : (
                <div className="relative">
                  <Building2 size={16} className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: 'var(--ink-soft)' }} />
                  <select
                    value={selectedTenantId || ''}
                    onChange={e => setSelectedTenantId(Number(e.target.value))}
                    className="input-field pl-9"
                    required
                    disabled={tenantsLoading}
                  >
                    <option value="" disabled>
                      {tenantsLoading ? 'Chargement...' : 'Sélectionner une académie'}
                    </option>
                    {tenants.map(t => (
                      <option key={t.id} value={t.id}>
                        {t.nom}{t.ville ? ` — ${t.ville}` : ''}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>

            <div>
              <label className="label">Email</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="input-field"
                placeholder="votre@email.com"
                required
                autoFocus
              />
            </div>
            <div>
              <label className="label">Mot de passe</label>
              <input
                type="password"
                value={motDePasse}
                onChange={e => setMotDePasse(e.target.value)}
                className="input-field"
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
            </div>
            {error && (
              <div className="text-sm px-3 py-2 rounded-lg" style={{ background: '#FBE7E7', color: 'var(--red)' }}>
                {error}
              </div>
            )}
            <button
              type="submit"
              disabled={loading || !selectedTenantId}
              className="btn-primary w-full flex items-center justify-center gap-2 py-3"
            >
              {loading ? (
                <span>Connexion...</span>
              ) : (
                <>
                  <LogIn size={18} />
                  Se connecter
                </>
              )}
            </button>
          </form>
        </div>

        {/* Demo mode card */}
        <div className="panel p-5 mt-4" style={{ border: '1px dashed var(--gold, #C9A227)' }}>
          <div className="flex items-center gap-2 mb-3">
            <Smartphone size={18} style={{ color: 'var(--gold, #C9A227)' }} />
            <h3 className="font-bebas text-lg" style={{ color: 'var(--pitch-dark)' }}>
              Mode Démo — Démonstration
            </h3>
          </div>
          <p className="text-xs mb-4" style={{ color: 'var(--ink-soft)' }}>
            Découvrez l'application avec des données pré-remplies. Aucun serveur requis.
          </p>
          <div className="flex flex-col gap-2">
            {demoRoles.map(r => (
              <button
                key={r.role}
                onClick={() => handleDemoRoleLogin(r.role)}
                className="flex items-center justify-between w-full px-4 py-2.5 rounded-lg text-sm font-medium transition-colors"
                style={{
                  background: demoRole === r.role ? 'var(--pitch-dark, #122A22)' : '#F0EEE4',
                  color: demoRole === r.role ? 'white' : 'var(--ink, #1A1A1A)',
                }}
              >
                <span>{r.label}</span>
                <span className="text-[11px] opacity-70">{r.desc}</span>
              </button>
            ))}
          </div>
        </div>

        {isDemo && (
          <button
            onClick={disableDemo}
            className="w-full mt-3 py-2 text-xs rounded-lg border"
            style={{ borderColor: 'var(--line)', color: 'var(--ink-soft)' }}
          >
            Quitter le mode démo
          </button>
        )}
      </div>
    </div>
  );
}
