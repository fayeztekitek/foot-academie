import { useState } from 'react';
import { Upload, FileSpreadsheet, Check, AlertCircle } from 'lucide-react';
import api from '../api/client';

export default function Import() {
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [importType, setImportType] = useState(null);

  const handleImport = async (type, file) => {
    if (!file) return;
    if (!/\.csv$/i.test(file.name) && file.type !== 'text/csv') {
      setError('Fichier CSV uniquement');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setError('Le fichier ne doit pas dépasser 5 Mo');
      return;
    }
    setImporting(true);
    setResult(null);
    setError(null);
    setImportType(type);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post(`/import/${type}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setResult(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'import');
    } finally {
      setImporting(false);
    }
  };

  return (
    <div>
      <h2 className="font-bebas text-2xl mb-5" style={{ color: 'var(--pitch-dark)' }}>Import de données</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Players import */}
        <div className="panel p-5">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: '#E6F1EA' }}>
              <FileSpreadsheet size={20} style={{ color: 'var(--grass)' }} />
            </div>
            <div>
              <h3 className="font-semibold text-sm" style={{ color: 'var(--ink)' }}>Joueurs</h3>
              <p className="text-xs" style={{ color: 'var(--ink-soft)' }}>Format CSV: prenom,nom,dateNaissance,categorie,parentEmail</p>
            </div>
          </div>
          <input
            type="file"
            accept=".csv"
            onChange={(e) => handleImport('players', e.target.files[0])}
            className="input-field"
            disabled={importing}
          />
        </div>

        {/* Payments import */}
        <div className="panel p-5">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: '#F1E9D6' }}>
              <FileSpreadsheet size={20} style={{ color: '#8A6A15' }} />
            </div>
            <div>
              <h3 className="font-semibold text-sm" style={{ color: 'var(--ink)' }}>Paiements</h3>
              <p className="text-xs" style={{ color: 'var(--ink-soft)' }}>Format CSV: joueurNom,joueurPrenom,montant,dateEcheance,statut</p>
            </div>
          </div>
          <input
            type="file"
            accept=".csv"
            onChange={(e) => handleImport('payments', e.target.files[0])}
            className="input-field"
            disabled={importing}
          />
        </div>
      </div>

      {importing && (
        <div className="flex items-center gap-2 mt-4 text-sm" style={{ color: 'var(--pitch)' }}>
          <span className="animate-spin h-5 w-5 border-2 border-current border-t-transparent rounded-full" />
          Import en cours...
        </div>
      )}

      {result && (
        <div className="panel mt-4 p-5" style={{ background: '#E6F1EA', borderColor: '#C5E0CC' }}>
          <div className="flex items-start gap-3">
            <Check size={20} className="mt-0.5" style={{ color: 'var(--grass)' }} />
            <div>
              <h4 className="font-medium text-sm" style={{ color: 'var(--grass)' }}>Import terminé</h4>
              <ul className="text-sm mt-1 space-y-0.5" style={{ color: '#1F5A3B' }}>
                {importType === 'players' && (
                  <>
                    <li>Joueurs importés: {result.joueursImported || 0}</li>
                    <li>Parents importés: {result.parentsImported || 0}</li>
                  </>
                )}
                {importType === 'payments' && (
                  <li>Paiements importés: {result.paiementsImported || 0}</li>
                )}
                {result.erreurs > 0 && <li style={{ color: 'var(--red)' }}>Erreurs: {result.erreurs}</li>}
              </ul>
              {result.messages?.length > 0 && (
                <div className="mt-2 text-xs" style={{ color: 'var(--ink-soft)' }}>
                  {result.messages.slice(0, 5).map((msg, i) => <div key={i}>{msg}</div>)}
                  {result.messages.length > 5 && <div>... et {result.messages.length - 5} autres</div>}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {error && (
        <div className="panel mt-4 p-5" style={{ background: '#FBE7E7', borderColor: '#F5C2C2' }}>
          <div className="flex items-start gap-3">
            <AlertCircle size={20} className="mt-0.5" style={{ color: 'var(--red)' }} />
            <div>
              <h4 className="font-medium text-sm" style={{ color: 'var(--red)' }}>Erreur d'import</h4>
              <p className="text-sm mt-1" style={{ color: '#8E1B2E' }}>{error}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
