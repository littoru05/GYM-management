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
      setAuth(data.user, data.accessToken)
      toast.success('Đăng nhập thành công')
      navigate(ROLE_REDIRECT[data.user.role] || '/', { replace: true })
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

  return () => {
    clearAuth()
    navigate('/login', { replace: true })
  }
}
