import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import api from '../api/client';
import { Shield, Download, CheckCircle, XCircle, FileText } from 'lucide-react';

export default function Rgpd() {
  const [exportDates, setExportDates] = useState({ start: '', end: '' });

  const { data: stats } = useQuery({
    queryKey: ['rgpd-stats'],
    queryFn: () => api.get('/rgpd/consents/stats').then(r => r.data),
  });

  const { data: consents = [] } = useQuery({
    queryKey: ['rgpd-consents'],
    queryFn: () => api.get('/rgpd/consents').then(r => r.data),
  });

  const handleExportPdf = () => {
    if (!exportDates.start || !exportDates.end) return;
    window.open(
      `${api.defaults.baseURL}/rgpd/consents/export?start=${exportDates.start}&end=${exportDates.end}`,
      '_blank'
    );
  };

  const formatType = (type) => {
    const map = {
      'TRAITEMENT_DONNEES_PERSONNELLES': 'Traitement données',
      'CONSENTEMENT_IMAGE': 'Consentement image',
      'COMMUNICATION_EMAIL': 'Communication email',
      'PARTENAIRES_TIERS': 'Partenaires tiers',
      'TRANSFERT_DONNEES_HORS_UE': 'Transfert hors UE',
    };
    return map[type] || type;
  };

  return (
    <div>
      <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>
        Registre de Consentement RGPD/INPDP
      </h2>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-6">
        <div className="panel p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: '#E6F1EA' }}>
              <Shield size={20} style={{ color: 'var(--grass)' }} />
            </div>
            <div>
              <p className="text-2xl font-bold" style={{ color: 'var(--ink)' }}>{stats?.total || 0}</p>
              <p className="text-xs" style={{ color: 'var(--ink-soft)' }}>Total consentements</p>
            </div>
          </div>
        </div>
        <div className="panel p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: '#E6F1EA' }}>
              <CheckCircle size={20} style={{ color: 'var(--grass)' }} />
            </div>
            <div>
              <p className="text-2xl font-bold" style={{ color: 'var(--ink)' }}>{stats?.granted || 0}</p>
              <p className="text-xs" style={{ color: 'var(--ink-soft)' }}>Accordés</p>
            </div>
          </div>
        </div>
        <div className="panel p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: '#FBE7E7' }}>
              <XCircle size={20} style={{ color: 'var(--red)' }} />
            </div>
            <div>
              <p className="text-2xl font-bold" style={{ color: 'var(--ink)' }}>{stats?.refused || 0}</p>
              <p className="text-xs" style={{ color: 'var(--ink-soft)' }}>Refusés</p>
            </div>
          </div>
        </div>
      </div>

      {/* PDF Export */}
      <div className="panel p-5 mb-6">
        <div className="flex items-center gap-3 mb-4">
          <FileText size={20} style={{ color: 'var(--pitch)' }} />
          <h3 className="font-semibold text-sm" style={{ color: 'var(--ink)' }}>Exporter en PDF</h3>
        </div>
        <div className="flex items-end gap-4">
          <div>
            <label className="block text-xs mb-1" style={{ color: 'var(--ink-soft)' }}>Du</label>
            <input type="date" value={exportDates.start}
              onChange={e => setExportDates({ ...exportDates, start: e.target.value })}
              className="input-field" />
          </div>
          <div>
            <label className="block text-xs mb-1" style={{ color: 'var(--ink-soft)' }}>Au</label>
            <input type="date" value={exportDates.end}
              onChange={e => setExportDates({ ...exportDates, end: e.target.value })}
              className="input-field" />
          </div>
          <button onClick={handleExportPdf} className="btn-primary flex items-center gap-2 text-sm">
            <Download size={14} /> Exporter PDF
          </button>
        </div>
      </div>

      {/* Consent Table */}
      <div className="panel overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr style={{ background: 'var(--pitch)' }}>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Parent</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Email</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Type</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Accordé</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Détails</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Date</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-white">Exporté</th>
              </tr>
            </thead>
            <tbody>
              {consents.map((c) => (
                <tr key={c.id} className="border-t" style={{ borderColor: 'var(--line)' }}>
                  <td className="px-4 py-3" style={{ color: 'var(--ink)' }}>{c.parentPrenom} {c.parentNom}</td>
                  <td className="px-4 py-3" style={{ color: 'var(--ink-soft)' }}>{c.parentEmail}</td>
                  <td className="px-4 py-3" style={{ color: 'var(--ink)' }}>{formatType(c.type)}</td>
                  <td className="px-4 py-3">
                    {c.granted
                      ? <span className="text-xs font-medium px-2 py-0.5 rounded" style={{ background: '#E6F1EA', color: 'var(--grass)' }}>Oui</span>
                      : <span className="text-xs font-medium px-2 py-0.5 rounded" style={{ background: '#FBE7E7', color: 'var(--red)' }}>Non</span>
                    }
                  </td>
                  <td className="px-4 py-3 text-xs" style={{ color: 'var(--ink-soft)' }}>{c.details || '—'}</td>
                  <td className="px-4 py-3 text-xs" style={{ color: 'var(--ink-soft)' }}>
                    {c.dateConsentement ? new Date(c.dateConsentement).toLocaleDateString('fr-TN') : '—'}
                  </td>
                  <td className="px-4 py-3">
                    {c.exported
                      ? <CheckCircle size={14} style={{ color: 'var(--grass)' }} />
                      : <span className="text-xs" style={{ color: 'var(--ink-soft)' }}>—</span>
                    }
                  </td>
                </tr>
              ))}
              {consents.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center" style={{ color: 'var(--ink-soft)' }}>
                    Aucun consentement enregistré
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
