import axios from 'axios'
import { showErrorToast } from '../utils/toastService.js'
import { useAuthStore } from '../store/useAuthStore.js'

/**
 * Safely extract Bearer token from client-side storage.
 * Prioritizes Zustand useAuthStore state, then falls back to direct localStorage / sessionStorage keys.
 */
export function getStoredToken() {
  try {
    // 1. Direct Zustand state
    const authState = useAuthStore.getState()
    if (authState?.accessToken) {
      return authState.accessToken
    }

    // 2. Direct localStorage keys
    if (typeof window !== 'undefined' && window.localStorage) {
      const directToken =
        window.localStorage.getItem('token') ||
        window.localStorage.getItem('accessToken')
      if (directToken) return directToken

      // Fallback: parse zustand persisted state key 'gym-auth'
      const persisted = window.localStorage.getItem('gym-auth')
      if (persisted) {
        const parsed = JSON.parse(persisted)
        if (parsed?.state?.accessToken) {
          return parsed.state.accessToken
        }
      }
    }

    // 3. Fallback to sessionStorage
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const sessionToken =
        window.sessionStorage.getItem('token') ||
        window.sessionStorage.getItem('accessToken')
      if (sessionToken) return sessionToken
    }
  } catch (err) {
    console.error('Lỗi khi đọc token từ storage:', err)
  }
  return null
}

/**
 * Safely wipe all authentication tokens and state from all storage sources.
 */
export function clearStoredAuth() {
  try {
    const { clearAuth } = useAuthStore.getState()
    if (typeof clearAuth === 'function') {
      clearAuth()
    }
  } catch (err) {
    console.error('Lỗi khi xóa Zustand auth:', err)
  }

  try {
    if (typeof window !== 'undefined') {
      window.localStorage?.removeItem('token')
      window.localStorage?.removeItem('accessToken')
      window.localStorage?.removeItem('gym-auth')
      window.sessionStorage?.removeItem('token')
      window.sessionStorage?.removeItem('accessToken')
    }
  } catch (err) {
    console.error('Lỗi khi xóa storage:', err)
  }
}

const axiosClient = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000, // 15s timeout: tránh treo kết nối khi ALB hoặc hạ tầng mạng ngắt quãng
  headers: {
    'Content-Type': 'application/json',
  },
})

// ==========================================
// 1. REQUEST INTERCEPTOR: Tự động trích xuất Bearer Token
// ==========================================
axiosClient.interceptors.request.use(
  (config) => {
    const token = getStoredToken()
    if (token) {
      if (config.headers && typeof config.headers.set === 'function') {
        config.headers.set('Authorization', `Bearer ${token}`)
      } else {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Flag to debounce redirection and prevent multiple overlapping redirects/toasts
let isRedirecting = false

// ==========================================
// 2. RESPONSE INTERCEPTOR: Bắt toàn bộ mã lỗi từ server & mạng
// ==========================================
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // 2.1. Bắt lỗi quá thời gian chờ (Timeout: ECONNABORTED / ETIMEDOUT / 504 Gateway Timeout)
    const isTimeout =
      error.code === 'ECONNABORTED' ||
      error.code === 'ETIMEDOUT' ||
      (typeof error.message === 'string' && error.message.toLowerCase().includes('timeout')) ||
      error.response?.status === 504

    if (isTimeout) {
      const timeoutMsg =
        'Hết thời gian chờ phản hồi từ máy chủ (Timeout). Đường truyền mạng tới ALB bị gián đoạn hoặc phản hồi chậm. Vui lòng thử lại.'
      showErrorToast(timeoutMsg, null, {
        id: 'network-timeout',
        duration: 5000,
      })
      error.userMessage = timeoutMsg
      return Promise.reject(error)
    }

    // 2.2. Bắt lỗi mất kết nối mạng (ERR_NETWORK / offline / server unreachable / ALB 502/503)
    const isNetworkError =
      error.code === 'ERR_NETWORK' ||
      error.code === 'ECONNRESET' ||
      error.code === 'ERR_CONNECTION_REFUSED' ||
      error.message === 'Network Error' ||
      (!error.response && Boolean(error.request)) ||
      (typeof window !== 'undefined' && !window.navigator?.onLine) ||
      error.response?.status === 502 ||
      error.response?.status === 503

    if (isNetworkError) {
      const networkMsg = 'Mất kết nối mạng (ERR_NETWORK). Vui lòng kiểm tra lại đường truyền internet.'
      showErrorToast(networkMsg, null, {
        id: 'network-error',
        duration: 5000,
      })
      error.userMessage = networkMsg
      return Promise.reject(error)
    }

    const status = error.response?.status
    const url = error.config?.url || ''
    const isAuthLoginRequest = url.includes('/auth/login') || url.includes('/login')

    // 2.2. Bắt mã 401 Unauthorized -> tự động xóa token và chuyển hướng về /login kèm thông báo
    if (status === 401) {
      // Nếu là request login bị sai thông tin, nhường quyền xử lý thông báo lỗi cụ thể cho form login
      if (!isAuthLoginRequest) {
        clearStoredAuth()

        const sessionExpiredMsg = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
        showErrorToast(sessionExpiredMsg, null, { id: 'auth-session-expired' })

        try {
          if (typeof window !== 'undefined') {
            window.sessionStorage?.setItem('auth_redirect_reason', sessionExpiredMsg)
            if (!window.location.pathname.includes('/login') && !isRedirecting) {
              isRedirecting = true
              setTimeout(() => {
                window.location.href = '/login'
              }, 150)
              // Reset flag sau 2s đề phòng trường hợp trang không reload
              setTimeout(() => {
                isRedirecting = false
              }, 2000)
            }
          }
        } catch {
          // Bỏ qua lỗi truy cập storage trên môi trường đặc biệt
        }
      }
      return Promise.reject(error)
    }

    // 2.3. Bắt mã 403 Forbidden -> cảnh báo không có quyền
    if (status === 403) {
      showErrorToast(error, 'Bạn không có quyền thực hiện thao tác này.', { id: 'auth-forbidden' })
      return Promise.reject(error)
    }

    // 2.4. Bắt mã lỗi 5xx Internal Server Error -> cảnh báo lỗi máy chủ
    if (status >= 500) {
      showErrorToast(
        error,
        `Máy chủ gặp sự cố (${status}). Vui lòng thử lại sau hoặc liên hệ quản trị viên.`,
        { id: `server-error-${status}` }
      )
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default axiosClient
