import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const isProd = mode === 'production'

  return {
    plugins: [react()],
    // Sử dụng bộ biến đổi OXC mặc định của Vite 8 để loại bỏ sạch console & debugger khi build production
    oxc: isProd
      ? {
          drop: ['console', 'debugger'],
        }
      : {},
    build: {
      target: 'es2020',
      cssCodeSplit: true,
      chunkSizeWarningLimit: 600,
      rollupOptions: {
        output: {
          manualChunks(id) {
            const normalizedId = id.replace(/\\/g, '/')
            if (normalizedId.includes('/node_modules/')) {
              // Nhóm React Core & Routing
              if (
                normalizedId.includes('/react/') ||
                normalizedId.includes('/react-dom/') ||
                normalizedId.includes('/react-router') ||
                normalizedId.includes('/react-router-dom/')
              ) {
                return 'vendor-react'
              }
              // Tách các module toán học & đồ thị d3 / victory-vendor riêng biệt
              if (
                normalizedId.includes('/d3-') ||
                normalizedId.includes('/victory-vendor/')
              ) {
                return 'vendor-charts-d3'
              }
              // Nhóm Recharts trực quan hóa dữ liệu
              if (normalizedId.includes('/recharts/')) {
                return 'vendor-charts'
              }
              // Nhóm Data Fetching & Network (Axios, React Query)
              if (
                normalizedId.includes('/@tanstack/react-query/') ||
                normalizedId.includes('/axios/')
              ) {
                return 'vendor-network'
              }
              // Nhóm UI Icons & Toast Notification
              if (
                normalizedId.includes('/lucide-react/') ||
                normalizedId.includes('/react-hot-toast/') ||
                normalizedId.includes('/clsx/')
              ) {
                return 'vendor-ui'
              }
              // Nhóm Form Validation (React Hook Form, Yup)
              if (
                normalizedId.includes('/react-hook-form/') ||
                normalizedId.includes('/yup/') ||
                normalizedId.includes('/@hookform/resolvers/')
              ) {
                return 'vendor-forms'
              }
              // Các vendor tiện ích còn lại (ví dụ zustand)
              return 'vendor-utils'
            }
          },
        },
      },

    },
  }
})

