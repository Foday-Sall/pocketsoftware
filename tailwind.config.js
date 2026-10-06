/** @type {import('tailwind.css').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#111110',
        card: '#181712',
        cardline: '#2A2820',
        fog: '#A8A293',
        sand: '#D9CFAF',
        paper: '#F7F4EC',
        honey: '#C9973B',
        honeyhi: '#E2B65C',
        moss: '#6C8A5B',
        wagreen: '#25D366',
      },
      fontFamily: {
        display: ['"Instrument Serif"', 'Georgia', 'serif'],
        body: ['"DM Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
};
