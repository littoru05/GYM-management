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
        <Toaster position="top-right" />
        <App />
      </QueryClientProvider>
    </ErrorBoundary>
  </StrictMode>,
)
