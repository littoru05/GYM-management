import axios from 'axios'
import { useAuthStore } from '../store/useAuthStore'

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const { accessToken, clearAuth } = useAuthStore.getState()
      if (accessToken && !window.location.pathname.includes('/login')) {
        clearAuth()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosClient
