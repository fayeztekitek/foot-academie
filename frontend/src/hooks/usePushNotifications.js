import { useEffect, useCallback } from 'react';
import { useAuth } from './useAuth';
import { initFirebase, registerDeviceToken, onForegroundMessage, fcmEnabled } from '../config/firebase';

export function usePushNotifications() {
  const { user } = useAuth();

  const setupPush = useCallback(async () => {
    if (!user || !fcmEnabled) return;

    const initialized = initFirebase();
    if (!initialized) return;

    await registerDeviceToken();
  }, [user]);

  useEffect(() => {
    setupPush();
  }, [setupPush]);

  useEffect(() => {
    if (!fcmEnabled) return;

    const unsubscribe = onForegroundMessage((payload) => {
      const { title, body } = payload.notification || {};
      if (title && 'serviceWorker' in navigator) {
        navigator.serviceWorker.ready.then((reg) => {
          reg.showNotification(title, {
            body: body || '',
            icon: '/logo-192.png',
            badge: '/logo-192.png',
            data: payload.data,
          });
        });
      }
    });

    return () => {
      if (typeof unsubscribe === 'function') unsubscribe();
    };
  }, []);
}
