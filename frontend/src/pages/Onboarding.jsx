import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { Building2, User, Mail, Lock, MapPin, Phone, CheckCircle, ArrowRight, ArrowLeft, Loader2 } from 'lucide-react';

const STEPS = [
  { id: 1, title: 'Votre académie', icon: Building2 },
  { id: 2, title: 'Votre compte admin', icon: User },
  { id: 3, title: 'Confirmation', icon: CheckCircle },
];

export default function Onboarding() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [form, setForm] = useState({
    academieNom: '',
    academieVille: '',
    academieAdresse: '',
    academieTelephone: '',
    adminEmail: '',
    adminPrenom: '',
    adminNom: '',
    adminMotDePasse: '',
  });

  const updateField = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    setError(null);
  };

  const validateStep1 = () => {
    if (!form.academieNom.trim()) {
      setError('Le nom de l\'académie est obligatoire');
      return false;
    }
    return true;
  };

  const validateStep2 = () => {
    if (!form.adminEmail.trim()) { setError('L\'email est obligatoire'); return false; }
    if (!form.adminPrenom.trim()) { setError('Le prénom est obligatoire'); return false; }
    if (!form.adminNom.trim()) { setError('Le nom est obligatoire'); return false; }
    if (!form.adminMotDePasse.trim()) { setError('Le mot de passe est obligatoire'); return false; }
    if (form.adminMotDePasse.length < 6) { setError('Le mot de passe doit contenir au moins 6 caractères'); return false; }
    return true;
  };

  const handleNext = () => {
    if (step === 1 && !validateStep1()) return;
    if (step === 2 && !validateStep2()) return;
    setStep(s => s + 1);
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.post('/onboarding', form);
      setSuccess(data);
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-lg w-full max-w-2xl overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-green-600 to-green-700 p-8 text-white">
          <div className="flex items-center gap-2 mb-1">
            <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center font-bold text-sm">N</div>
            <span className="font-bebas text-xl">NADI</span>
          </div>
          <h1 className="text-2xl font-bold mt-3">Créez votre académie</h1>
          <p className="text-green-100 mt-1">En quelques étapes simples</p>
        </div>

        {/* Progress */}
        <div className="flex items-center justify-center gap-2 py-4 border-b">
          {STEPS.map((s, i) => (
            <div key={s.id} className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${
                step >= s.id ? 'bg-green-600 text-white' : 'bg-gray-200 text-gray-500'
              }`}>
                {step > s.id ? <CheckCircle size={16} /> : s.id}
              </div>
              <span className={`text-sm hidden sm:inline ${step >= s.id ? 'text-gray-900 font-medium' : 'text-gray-400'}`}>
                {s.title}
              </span>
              {i < STEPS.length - 1 && <div className={`w-8 h-0.5 ${step > s.id ? 'bg-green-600' : 'bg-gray-200'}`} />}
            </div>
          ))}
        </div>

        {/* Content */}
        <div className="p-8">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">{error}</div>
          )}

          {step === 1 && (
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Informations de l'académie</h2>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nom de l'académie *</label>
                <div className="relative">
                  <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                  <input type="text" value={form.academieNom} onChange={(e) => updateField('academieNom', e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    placeholder="Ex: Académie Stars" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ville</label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input type="text" value={form.academieVille} onChange={(e) => updateField('academieVille', e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                      placeholder="Tunis" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input type="text" value={form.academieTelephone} onChange={(e) => updateField('academieTelephone', e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                      placeholder="+216 71 000 000" />
                  </div>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Adresse</label>
                <input type="text" value={form.academieAdresse} onChange={(e) => updateField('academieAdresse', e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="123 Rue de la Paix, Tunis" />
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Compte administrateur</h2>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Prénom *</label>
                  <input type="text" value={form.adminPrenom} onChange={(e) => updateField('adminPrenom', e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                  <input type="text" value={form.adminNom} onChange={(e) => updateField('adminNom', e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                  <input type="email" value={form.adminEmail} onChange={(e) => updateField('adminEmail', e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    placeholder="admin@monacademie.tn" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mot de passe *</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                  <input type="password" value={form.adminMotDePasse} onChange={(e) => updateField('adminMotDePasse', e.target.value)}
                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500"
                    autoComplete="new-password" minLength={6} />
                </div>
              </div>
            </div>
          )}

          {step === 3 && success && (
            <div className="text-center py-6">
              <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="text-green-600" size={40} />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Félicitations !</h2>
              <p className="text-gray-500 mb-2">Votre académie <strong>{success.academieNom}</strong> a été créée.</p>
              <p className="text-sm text-gray-400 mb-6">Connectez-vous avec <strong>{success.adminEmail}</strong></p>
              <button
                onClick={() => navigate('/login')}
                className="bg-green-600 text-white px-8 py-3 rounded-lg font-medium hover:bg-green-700 transition"
              >
                Se connecter
              </button>
            </div>
          )}
        </div>

        {/* Footer */}
        {step < 3 && (
          <div className="px-8 pb-6 flex justify-between">
            {step > 1 ? (
              <button onClick={() => setStep(s => s - 1)}
                className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 transition">
                <ArrowLeft size={16} /> Retour
              </button>
            ) : <div />}
            {step === 2 ? (
              <button onClick={handleSubmit} disabled={loading}
                className="flex items-center gap-2 bg-green-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-green-700 transition disabled:opacity-50">
                {loading ? <Loader2 className="animate-spin" size={16} /> : <CheckCircle size={16} />}
                {loading ? 'Création...' : 'Créer l\'académie'}
              </button>
            ) : (
              <button onClick={handleNext}
                className="flex items-center gap-2 bg-green-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-green-700 transition">
                Suivant <ArrowRight size={16} />
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
