import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CalendarPlus, Copy, RefreshCw, Check } from 'lucide-react';
import { calendarApi } from '../api';
import { getBackendUrl } from '../demo/demoConfig';

export default function CalendarSubscription({ compact = false }) {
  const queryClient = useQueryClient();
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState('');

  const { data } = useQuery({
    queryKey: ['calendar-token'],
    queryFn: () => calendarApi.getToken(),
  });

  const regenerateMutation = useMutation({
    mutationFn: () => calendarApi.regenerate(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendar-token'] });
      setError('');
    },
    onError: () => setError('Régénération impossible'),
  });

  const token = data?.token || '';
  const feedUrl = token ? `${getBackendUrl()}/api/calendar/feed?token=${token}` : '';

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(feedUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setError('Copie impossible — copiez le lien manuellement');
    }
  };

  return (
    <div className={compact ? '' : 'panel p-5 mb-6'}>
      <div className="flex items-center gap-2 mb-2">
        <CalendarPlus size={18} style={{ color: 'var(--grass)' }} />
        <h3 className="font-bebas text-lg m-0" style={{ color: 'var(--pitch-dark)' }}>
          Calendrier synchronisé
        </h3>
      </div>
      <p className="text-xs mb-3" style={{ color: 'var(--ink-soft)' }}>
        Abonnez votre agenda (Google, Apple, Outlook) : les nouveaux événements et horaires s'y ajoutent automatiquement.
      </p>
      {token ? (
        <>
          <div className="flex gap-2">
            <input
              type="text"
              readOnly
              value={feedUrl}
              onFocus={e => e.target.select()}
              className="input-field text-xs font-mono flex-1 min-w-0"
            />
            <button type="button" onClick={copy} className="btn-ghost btn-sm flex items-center gap-1 whitespace-nowrap">
              {copied ? <Check size={14} /> : <Copy size={14} />}
              {copied ? 'Copié' : 'Copier'}
            </button>
          </div>
          <button
            type="button"
            onClick={() => { if (window.confirm('Régénérer le lien ? L\'ancien lien cessera de fonctionner.')) regenerateMutation.mutate(); }}
            disabled={regenerateMutation.isPending}
            className="flex items-center gap-1 text-[11px] mt-2 underline"
            style={{ color: 'var(--ink-soft)' }}
          >
            <RefreshCw size={11} /> Régénérer le lien (révoque l'ancien)
          </button>
        </>
      ) : (
        <button
          type="button"
          onClick={() => regenerateMutation.mutate()}
          disabled={regenerateMutation.isPending}
          className="btn-primary btn-sm text-sm"
        >
          {regenerateMutation.isPending ? 'Création…' : 'Activer la synchronisation'}
        </button>
      )}
      {error && (
        <p className="text-xs mt-2" style={{ color: 'var(--red)' }}>{error}</p>
      )}
    </div>
  );
}
