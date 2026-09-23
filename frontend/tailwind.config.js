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
          dark: 'var(--color-pitch-dark)',
          DEFAULT: 'var(--color-pitch)',
        },
        grass: {
          DEFAULT: 'var(--color-grass)',
          light: 'var(--color-grass-light)',
          dark: 'var(--primitive-color-grass-dark)',
        },
        gold: {
          DEFAULT: 'var(--color-gold)',
          light: 'var(--color-gold-light)',
        },
        red: {
          DEFAULT: 'var(--color-red)',
          light: 'var(--color-red-light)',
          dark: 'var(--primitive-color-red-dark)',
        },
        blue: {
          DEFAULT: 'var(--color-blue)',
          light: 'var(--color-blue-light)',
        },
        ink: {
          DEFAULT: 'var(--color-ink)',
          soft: 'var(--color-ink-soft)',
          muted: 'var(--color-ink-muted)',
        },
        line: {
          DEFAULT: 'var(--color-line)',
          strong: 'var(--color-line-strong)',
        },
        bg: 'var(--color-bg)',
        card: 'var(--color-card)',
      },
      fontFamily: {
        bebas: ['"Bebas Neue"', 'sans-serif'],
        worksans: ['"Work Sans"', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
        mono: ['"Roboto Mono"', 'monospace'],
      },
      borderRadius: {
        'lg': 'var(--primitive-radius-lg)',
        'xl': 'var(--primitive-radius-xl)',
        '2xl': 'var(--primitive-radius-2xl)',
      },
      boxShadow: {
        'card': 'var(--card-shadow)',
        'card-hover': 'var(--card-shadow-hover)',
        'modal': 'var(--primitive-shadow-modal)',
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
