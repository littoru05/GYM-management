import axios from 'axios'
import toast from 'react-hot-toast'
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
    // 2.1. Bắt lỗi mất kết nối mạng (ERR_NETWORK / offline / server unreachable)
    const isNetworkError =
      error.code === 'ERR_NETWORK' ||
      error.message === 'Network Error' ||
      (!error.response && Boolean(error.request)) ||
      (typeof window !== 'undefined' && !window.navigator?.onLine)

    if (isNetworkError) {
      const networkMsg = 'Mất kết nối mạng (ERR_NETWORK). Vui lòng kiểm tra lại đường truyền internet.'
      toast.error(networkMsg, {
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
        toast.error(sessionExpiredMsg, { id: 'auth-session-expired' })

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
      const forbiddenMsg =
        error.response?.data?.message || 'Bạn không có quyền thực hiện thao tác này.'
      toast.error(forbiddenMsg, { id: 'auth-forbidden' })
      return Promise.reject(error)
    }

    // 2.4. Bắt mã lỗi 5xx Internal Server Error -> cảnh báo lỗi máy chủ
    if (status >= 500) {
      const serverMsg =
        error.response?.data?.message ||
        `Máy chủ gặp sự cố (${status}). Vui lòng thử lại sau hoặc liên hệ quản trị viên.`
      toast.error(serverMsg, { id: `server-error-${status}` })
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default axiosClient
