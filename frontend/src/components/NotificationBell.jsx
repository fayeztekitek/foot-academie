import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Bell, CheckCheck } from 'lucide-react';
import { notificationsApi } from '../api/notifications';

function timeAgo(iso) {
  if (!iso) return '';
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 60) return "à l'instant";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `il y a ${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `il y a ${hours} h`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `il y a ${days} j`;
  return new Date(iso).toLocaleDateString('fr-FR');
}

const TYPE_DOT = {
  COMPETITION: '#2563EB',
  PAIEMENT_EN_RETARD: '#DC3545',
  PAIEMENT_RAPPEL: '#C9A227',
  DOCUMENT_EXPIRE: '#DC3545',
  DOCUMENT_EXPIRE_BIENTOT: '#C9A227',
  CHANGEMENT_HORAIRE: '#7C3AED',
};

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const queryClient = useQueryClient();

  const { data: unread } = useQuery({
    queryKey: ['notifications-unread'],
    queryFn: () => notificationsApi.getUnreadCount().then(r => r.data),
    refetchInterval: 30000,
  });

  const { data: page, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationsApi.getAll({ page: 0, size: 20 }).then(r => r.data),
    enabled: open,
  });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['notifications'] });
    queryClient.invalidateQueries({ queryKey: ['notifications-unread'] });
  };

  const markAll = useMutation({
    mutationFn: () => notificationsApi.markAllAsRead(),
    onSuccess: refresh,
  });

  const markOne = useMutation({
    mutationFn: (id) => notificationsApi.markAsRead(id),
    onSuccess: refresh,
  });

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open ]);

  const count = unread?.count || 0;
  const items = page?.content || page || [];

  return (
    <div ref={ref} className="relative w-full h-full flex items-center justify-center">
      <button
        type="button"
        onClick={() => setOpen(o => !o)}
        className="w-full h-full flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
        aria-label="Notifications"
      >
        <Bell size={18} className="text-ink-soft" />
        {count > 0 && (
          <span className="absolute top-0.5 right-0.5 min-w-[18px] h-[18px] px-0.5 bg-red text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white">
            {count > 9 ? '9+' : count}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-2 w-[320px] max-w-[85vw] max-h-[380px] flex flex-col bg-white rounded-xl shadow-xl border z-[100] overflow-hidden"
          style={{ borderColor: 'var(--line)' }}>
          <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: 'var(--line)' }}>
            <span className="font-semibold text-sm" style={{ color: 'var(--pitch-dark)' }}>Notifications</span>
            {count > 0 && (
              <button
                type="button"
                onClick={() => markAll.mutate()}
                disabled={markAll.isPending}
                className="flex items-center gap-1 text-xs font-medium"
                style={{ color: 'var(--grass)' }}
              >
                <CheckCheck size={14} /> Tout marquer comme lu
              </button>
            )}
          </div>
          <div className="overflow-y-auto flex-1">
            {isLoading ? (
              <p className="text-sm text-center py-8" style={{ color: 'var(--ink-soft)' }}>Chargement…</p>
            ) : items.length === 0 ? (
              <p className="text-sm text-center py-8" style={{ color: 'var(--ink-soft)' }}>Aucune notification</p>
            ) : (
              items.map(n => (
                <button
                  key={n.id}
                  type="button"
                  onClick={() => { if (!n.lu) markOne.mutate(n.id); }}
                  className="w-full text-left px-4 py-3 border-b last:border-0 hover:bg-gray-50 transition-colors"
                  style={{
                    borderColor: 'var(--line)',
                    background: n.lu ? undefined : 'var(--blue-light, #EFF4FF)',
                  }}
                >
                  <div className="flex items-start gap-2.5">
                    <span
                      className="mt-1.5 w-2 h-2 rounded-full flex-shrink-0"
                      style={{ background: n.lu ? 'var(--line)' : (TYPE_DOT[n.type] || 'var(--grass)') }}
                    />
                    <span className="flex-1 min-w-0">
                      <span className="block text-[13px] font-medium leading-snug" style={{ color: 'var(--ink)' }}>
                        {n.message}
                      </span>
                      {n.details && (
                        <span className="block text-xs mt-0.5 leading-snug" style={{ color: 'var(--ink-soft)' }}>
                          {n.details}
                        </span>
                      )}
                      <span className="block text-[11px] mt-1" style={{ color: 'var(--ink-muted)' }}>
                        {timeAgo(n.dateEnvoi)}
                      </span>
                    </span>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
