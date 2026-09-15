import { useQuery } from '@tanstack/react-query';
import { notificationsApi } from '../api/notifications';

export default function NotificationBell() {
  const { data } = useQuery({
    queryKey: ['notifications-unread'],
    queryFn: () => notificationsApi.getUnreadCount().then(r => r.data),
    refetchInterval: 30000,
  });

  const count = data?.count || 0;

  if (count === 0) return null;

  return (
    <span className="absolute -top-1 -right-1 w-[18px] h-[18px] bg-red text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white">
      {count > 9 ? '9+' : count}
    </span>
  );
}
