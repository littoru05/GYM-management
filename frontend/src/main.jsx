import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import ErrorBoundary from './components/common/ErrorBoundary.jsx'
import './index.css'
import App from './App.jsx'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // Không retry các mã lỗi xác thực hoặc không tìm thấy để phản hồi mượt mà
        const status = error?.response?.status
        if (status === 401 || status === 403 || status === 404) {
          return false
        }
        return failureCount < 1
      },
      refetchOnWindowFocus: false,
    },
  },
})

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <Toaster
          position="top-right"
          gutter={10}
          containerStyle={{
            top: 24,
            right: 24,
          }}
          toastOptions={{
            duration: 4000,
            style: {
              background: '#FFFFFF',
              color: '#1F2937',
              borderRadius: '14px',
              padding: '12px 18px',
              fontSize: '14px',
              fontWeight: '500',
              lineHeight: '1.45',
              border: '1px solid #E5E7EB',
              boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.08)',
            },
            // Toast xanh: Thông báo thành công (tạo mới, cập nhật, xóa)
            success: {
              duration: 3500,
              style: {
                background: '#F0FDF4',
                color: '#15803D',
                border: '1px solid #86EFAC',
              },
              iconTheme: {
                primary: '#16A34A',
                secondary: '#FFFFFF',
              },
            },
            // Toast đỏ: Cảnh báo lỗi nghiệp vụ dựa trên message backend trả về
            error: {
              duration: 5000,
              style: {
                background: '#FEF2F2',
                color: '#B91C1C',
                border: '1px solid #FCA5A5',
              },
              iconTheme: {
                primary: '#DC2626',
                secondary: '#FFFFFF',
              },
            },
          }}
        />
        <App />
      </QueryClientProvider>
    </ErrorBoundary>
  </StrictMode>,
)
