import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

const firebaseConfig = {
  apiKey: process.env.VITE_FIREBASE_API_KEY || '',
  authDomain: process.env.VITE_FIREBASE_AUTH_DOMAIN || '',
  projectId: process.env.VITE_FIREBASE_PROJECT_ID || '',
  storageBucket: process.env.VITE_FIREBASE_STORAGE_BUCKET || '',
  messagingSenderId: process.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '',
  appId: process.env.VITE_FIREBASE_APP_ID || '',
}

export default defineConfig({
  define: {
    'self.__FIREBASE_CONFIG__': JSON.stringify(firebaseConfig),
    __VITE_API_URL__: JSON.stringify(process.env.VITE_API_URL || ''),
  },
  plugins: [
    react(),
    VitePWA({
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
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https?:\/\/.*\/api\/(slots|categories|players|coaches|dashboard|parents\/me).*/,
            handler: 'NetworkFirst',
            method: 'GET',
            options: {
              cacheName: 'api-read-cache',
              expiration: { maxEntries: 100, maxAgeSeconds: 3600 },
              networkTimeoutSeconds: 5
            }
          },
          {
            urlPattern: /^https?:\/\/.*\/api\/notifications.*/,
            handler: 'NetworkFirst',
            method: 'GET',
            options: {
              cacheName: 'notifications-cache',
              expiration: { maxEntries: 50, maxAgeSeconds: 1800 },
              networkTimeoutSeconds: 3
            }
          }
        ]
      }
    })
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
})
