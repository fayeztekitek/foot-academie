import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig(({ mode }) => {
  // Load .env files (.env.production etc.) + OS env. OS env wins when set
  // (e.g. Vercel dashboard vars, Docker ENV). Previously the code read only
  // process.env, so `.env.production` was silently ignored and native builds
  // shipped with an empty API URL (all calls went to https://localhost/api).
  const env = loadEnv(mode, process.cwd(), '')
  const apiUrl = process.env.VITE_API_URL || env.VITE_API_URL || ''

  const firebaseConfig = {
    apiKey: process.env.VITE_FIREBASE_API_KEY || env.VITE_FIREBASE_API_KEY || '',
    authDomain: process.env.VITE_FIREBASE_AUTH_DOMAIN || env.VITE_FIREBASE_AUTH_DOMAIN || '',
    projectId: process.env.VITE_FIREBASE_PROJECT_ID || env.VITE_FIREBASE_PROJECT_ID || '',
    storageBucket: process.env.VITE_FIREBASE_STORAGE_BUCKET || env.VITE_FIREBASE_STORAGE_BUCKET || '',
    messagingSenderId: process.env.VITE_FIREBASE_MESSAGING_SENDER_ID || env.VITE_FIREBASE_MESSAGING_SENDER_ID || '',
    appId: process.env.VITE_FIREBASE_APP_ID || env.VITE_FIREBASE_APP_ID || '',
  }

  // Service workers must NOT ship inside the native APK: the Capacitor
  // WebView reuses origin https://localhost across installs, so a cached
  // index.html can reference deleted hashed assets => permanent blank screen.
  // Build native dist with CAPACITOR_BUILD=true. Web (Vercel) keeps the PWA.
  const isCapacitor = (process.env.CAPACITOR_BUILD || env.CAPACITOR_BUILD) === 'true'

  return {
  define: {
    'self.__FIREBASE_CONFIG__': JSON.stringify(firebaseConfig),
    __VITE_API_URL__: JSON.stringify(apiUrl),
  },
  plugins: [
    react(),
    ...(isCapacitor ? [] : [VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'logo-192.png', 'logo-512.png'],
      manifest: {
        name: 'Nadi — Gestion Académie de Football',
        short_name: 'Nadi',
        description: 'Application de gestion pour académie de football',
        theme_color: '#122A22',
        background_color: '#F5F3EC',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        categories: ['sports', 'education'],
        icons: [
          { src: 'logo-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'logo-512.png', sizes: '512x512', type: 'image/png', purpose: 'any maskable' }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}']
        // No runtimeCaching for API responses: tenant-scoped PII (players,
        // parents/me, dashboard, notifications) was cached keyed by URL only,
        // so a slow network could serve user A's records to user B on a
        // shared profile, and any XSS on the origin could read the caches.
      }
    })])
  ],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
  }
})
