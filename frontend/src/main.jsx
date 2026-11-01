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
        // Không retry các mã lỗi client 4xx (400, 401, 403, 404, 409, 422)
        const status = error?.response?.status
        if (status && status >= 400 && status < 500) {
          return false
        }
        // Thử lại nhẹ nhàng tối đa 2 lần đối với lỗi mạng, timeout (ECONNABORTED) hoặc 5xx từ máy chủ/ALB
        return failureCount < 2
      },
      // Giãn cách thử lại nhẹ nhàng (exponential backoff: 1s -> 1.5s -> tối đa 4s)
      retryDelay: (attemptIndex) => Math.min(1000 * Math.pow(1.5, attemptIndex), 4000),
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
