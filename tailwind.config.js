/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0B0B10',
        card: '#13131A',
        cardline: '#1E293B',
        fog: '#94A3B8',
        star: '#F8FAFC',
        launch: '#3B82F6',
        launchdeep: '#2563EB',
        wa: '#25D366',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"DM Sans"', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
