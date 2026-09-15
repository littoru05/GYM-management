/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#fff3ed',
          100: '#ffe4d5',
          200: '#ffc4aa',
          300: '#ff9d74',
          400: '#ff6b3d',
          500: '#f8471a',
          600: '#e8340f',
          700: '#c1260d',
          800: '#9a2012',
          900: '#7c1d12',
        },
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(16, 24, 40, 0.08), 0 1px 2px -1px rgba(16, 24, 40, 0.08)',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: 0, transform: 'translateY(4px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}

