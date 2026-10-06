/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // light Swiss theme per ui-ux-pro-max "Chat & Messaging App" palette
        ink: '#0F172A',        // dark slate: text on light, dark chat panels
        card: '#FFFFFF',       // cards on white bg
        cardline: '#E4ECFC',   // soft blue borders
        fog: '#475569',        // muted body text
        sand: '#475569',       // secondary text (light-context)
        paper: '#0F172A',      // primary text
        honey: '#2563EB',      // primary messenger blue
        honeyhi: '#1D4ED8',    // deep blue for large display text
        moss: '#059669',       // online green accent
        wagreen: '#25D366',    // WhatsApp brand only
      },
      fontFamily: {
        display: ['"Poppins"', 'sans-serif'],
        body: ['"Open Sans"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
};
