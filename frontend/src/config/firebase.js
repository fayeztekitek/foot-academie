import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import api from '../api/client';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || '',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || '',
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || '',
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || '',
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '',
};

let messaging = null;
let fcmEnabled = false;

export function initFirebase() {
  if (!firebaseConfig.apiKey || !firebaseConfig.projectId) {
    console.log('Firebase config not found — push notifications disabled');
    return false;
  }
  try {
    const app = initializeApp(firebaseConfig);
    messaging = getMessaging(app);
    fcmEnabled = true;
    return true;
  } catch (e) {
    console.warn('Firebase init failed:', e.message);
    return false;
  }
}

export async function requestPushPermission() {
  if (!fcmEnabled || !messaging) return null;

  try {
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') return null;

    const token = await getToken(messaging, {
      vapidKey: import.meta.env.VITE_FIREBASE_VAPID_KEY || undefined,
    });
    return token;
  } catch (e) {
    console.warn('FCM token request failed:', e.message);
    return null;
  }
}

export async function registerDeviceToken() {
  const token = await requestPushPermission();
  if (!token) return;

  try {
    await api.post('/notifications/register-device', {
      token,
      platform: 'web',
    });
    console.log('FCM device registered');
  } catch (e) {
    console.warn('Device registration failed:', e.message);
  }
}

export function onForegroundMessage(callback) {
  if (!fcmEnabled || !messaging) return () => {};
  return onMessage(messaging, callback);
}

export { messaging, fcmEnabled };
