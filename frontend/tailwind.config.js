/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        pitch: {
          dark: '#0F1D17',
          DEFAULT: '#1A3A2D',
        },
        grass: {
          DEFAULT: '#22884E',
          light: '#E6F5EC',
          dark: '#1D7A44',
        },
        gold: {
          DEFAULT: '#C9A227',
          light: '#FBF5E0',
        },
        red: {
          DEFAULT: '#DC3545',
          light: '#FDE8EA',
          dark: '#B91C1C',
        },
        blue: {
          DEFAULT: '#2563EB',
          light: '#EFF4FF',
        },
        ink: {
          DEFAULT: '#111827',
          soft: '#6B7280',
          muted: '#9CA3AF',
        },
        line: {
          DEFAULT: '#E5E7EB',
          strong: '#D1D5DB',
        },
        bg: '#F7F6F3',
        card: '#FFFFFF',
      },
      fontFamily: {
        bebas: ['"Bebas Neue"', 'sans-serif'],
        worksans: ['"Work Sans"', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
        mono: ['"Roboto Mono"', 'monospace'],
      },
      borderRadius: {
        'lg': '10px',
        'xl': '14px',
        '2xl': '20px',
      },
      boxShadow: {
        'card': '0 1px 2px rgba(0,0,0,.04)',
        'card-hover': '0 4px 6px -1px rgba(0,0,0,.07), 0 2px 4px -1px rgba(0,0,0,.04)',
        'modal': '0 20px 25px -5px rgba(0,0,0,.1), 0 10px 10px -5px rgba(0,0,0,.04)',
      },
      animation: {
        'slide-up': 'slide-up .25s ease-out',
        'scale-in': 'scale-in .2s ease-out',
        'fade-in': 'fade-in .2s ease-out',
        'shimmer': 'skeleton-shimmer 1.5s ease-in-out infinite',
      },
      keyframes: {
        'slide-up': {
          '0%': { transform: 'translateY(8px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        'scale-in': {
          '0%': { transform: 'scale(.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'skeleton-shimmer': {
          '0%': { backgroundPosition: '200% 0' },
          '100%': { backgroundPosition: '-200% 0' },
        },
      },
    },
  },
  plugins: [],
}
