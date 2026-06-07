/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        apple: {
          blue: '#0071e3',
          gray: '#86868b',
          light: '#f5f5f7',
          dark: '#1d1d1f',
          border: '#d2d2d7',
        },
      },
    },
  },
  plugins: [],
};
