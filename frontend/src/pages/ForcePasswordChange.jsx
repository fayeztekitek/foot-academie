import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { Shield, Eye, EyeOff } from 'lucide-react';

export default function ForcePasswordChange() {
  const { changePassword, logout } = useAuth();
  const [ancien, setAncien] = useState('');
  const [nouveau, setNouveau] = useState('');
  const [confirmer, setConfirmer] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showAncien, setShowAncien] = useState(false);
  const [showNouveau, setShowNouveau] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (nouveau.length < 6) {
      setError('Le mot de passe doit contenir au moins 6 caractères');
      return;
    }
    if (nouveau !== confirmer) {
      setError('Les mots de passe ne correspondent pas');
      return;
    }
    if (ancien === nouveau) {
      setError('Le nouveau mot de passe doit être différent de l\'ancien');
      return;
    }

    setLoading(true);
    try {
      await changePassword(ancien, nouveau);
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Erreur lors du changement de mot de passe');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-5" style={{ background: 'linear-gradient(135deg, #E6F1EA 0%, #dcfce7 100%)' }}>
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-[60px] h-[60px] rounded-full mx-auto mb-4 flex items-center justify-center"
            style={{ background: 'var(--pitch-dark, #122A22)' }}>
            <Shield size={28} className="text-white" />
          </div>
          <h1 className="font-bebas text-3xl mb-1" style={{ color: 'var(--pitch-dark)' }}>Changement de mot de passe</h1>
          <p className="text-sm" style={{ color: 'var(--ink-soft)' }}>
            Pour votre sécurité, vous devez modifier votre mot de passe avant de continuer.
          </p>
        </div>

        <div className="panel p-6">
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div>
              <label className="label">Mot de passe actuel</label>
              <div className="relative">
                <input
                  type={showAncien ? 'text' : 'password'}
                  value={ancien}
                  onChange={e => setAncien(e.target.value)}
                  className="input-field pr-10"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  required
                  autoFocus
                />
                <button type="button" onClick={() => setShowAncien(!showAncien)}
                  className="absolute right-3 top-3" style={{ color: 'var(--ink-soft)' }}>
                  {showAncien ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div>
              <label className="label">Nouveau mot de passe</label>
              <div className="relative">
                <input
                type={showNouveau ? 'text' : 'password'}
                value={nouveau}
                onChange={e => setNouveau(e.target.value)}
                className="input-field pr-10"
                placeholder="Min. 6 caractères"
                autoComplete="new-password"
                required
                />
                <button type="button" onClick={() => setShowNouveau(!showNouveau)}
                  className="absolute right-3 top-3" style={{ color: 'var(--ink-soft)' }}>
                  {showNouveau ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div>
              <label className="label">Confirmer le mot de passe</label>
              <input
                type="password"
                value={confirmer}
                onChange={e => setConfirmer(e.target.value)}
                className="input-field"
                placeholder="Retapez le mot de passe"
                autoComplete="new-password"
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
              disabled={loading}
              className="btn-primary w-full py-3"
            >
              {loading ? 'Enregistrement...' : 'Changer le mot de passe'}
            </button>
          </form>

          <div className="mt-4 pt-3 border-t text-center" style={{ borderColor: 'var(--line)' }}>
            <button onClick={logout} className="text-xs" style={{ color: 'var(--ink-soft)' }}>
              Se déconnecter
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
