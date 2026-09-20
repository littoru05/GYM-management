import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import * as authService from '../services/authService'
import { useAuthStore } from '../store/useAuthStore'

const ROLE_REDIRECT = {
  ADMIN: '/admin/dashboard',
  STAFF: '/staff/pos',
}

export function useLogin() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

  return useMutation({
    mutationFn: ({ email, password }) => authService.login(email, password),
    onSuccess: (data) => {
      const user = {
        id: data.id,
        code: data.code,
        name: data.name,
        email: data.email,
        phone: data.phone,
        role: data.role,
        status: data.status,
      }
      setAuth(user, data.token)
      toast.success('Đăng nhập thành công')
      navigate(ROLE_REDIRECT[data.role] || '/', { replace: true })
    },
    onError: (error) => {
      const message = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại.'
      toast.error(message)
    },
  })
}

export function useLogout() {
  const navigate = useNavigate()
  const clearAuth = useAuthStore((state) => state.clearAuth)

  return async () => {
    try {
      await authService.logout()
    } catch {
      // Token đã hết hạn hoặc mất kết nối vẫn phải cho phép đăng xuất ở client
    }
    clearAuth()
    navigate('/login', { replace: true })
  }
}
