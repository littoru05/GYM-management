import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { showErrorToast } from '../../utils/toastService'
import { Dumbbell, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-react'
import Input from '../../components/common/Input'
import Button from '../../components/common/Button'
import { useLogin } from '../../hooks/useAuth'

const schema = yup.object({
  email: yup
    .string()
    .required('Email không được để trống')
    .email('Email không đúng định dạng'),
  password: yup.string().required('Mật khẩu không được để trống'),
})

function LoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [searchParams] = useSearchParams()
  const [redirectNotice, setRedirectNotice] = useState(() => {
    try {
      const reasonFromStorage = typeof window !== 'undefined' ? sessionStorage.getItem('auth_redirect_reason') : null
      const reasonFromParam = searchParams.get('reason') || (searchParams.get('expired') === 'true' ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.' : null)
      return reasonFromStorage || reasonFromParam || null
    } catch {
      return null
    }
  })
  const loginMutation = useLogin()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema) })

  useEffect(() => {
    if (redirectNotice) {
      showErrorToast(redirectNotice, null, { id: 'auth-session-expired' })
      try {
        sessionStorage.removeItem('auth_redirect_reason')
      } catch {
        // Ignore storage errors
      }
    }
  }, [redirectNotice])

  const onSubmit = (values) => {
    setRedirectNotice(null)
    try {
      sessionStorage.removeItem('auth_redirect_reason')
    } catch {
      // Ignore
    }
    loginMutation.mutate(values)
  }

  const displayNotice = loginMutation.isError ? null : redirectNotice

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 shadow-card">
        <div className="flex flex-col items-center gap-2">
          <div className="flex h-11 w-11 items-center justify-center rounded-lg bg-primary-500 text-white">
            <Dumbbell size={22} />
          </div>
          <span className="text-lg font-bold text-gray-900">PowerFit Gym</span>
        </div>

        <h1 className="mt-6 text-center text-xl font-bold text-gray-900">Đăng nhập</h1>
        <p className="mt-1 text-center text-sm text-gray-500">Đăng nhập để quản lý phòng gym của bạn</p>

        {displayNotice && (
          <div className="mt-4 flex items-start gap-2.5 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
            <AlertCircle size={16} className="mt-0.5 flex-shrink-0 text-amber-600" />
            <span>{displayNotice}</span>
          </div>
        )}

        <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
          <Input
            label="Email / Tài khoản"
            type="email"
            icon={Mail}
            placeholder="you@gym.local"
            error={errors.email?.message}
            {...register('email')}
          />

          <Input
            label="Mật khẩu"
            type={showPassword ? 'text' : 'password'}
            icon={Lock}
            endIcon={showPassword ? EyeOff : Eye}
            endIconLabel={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
            onEndIconClick={() => setShowPassword((v) => !v)}
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password')}
          />

          <Button type="submit" fullWidth loading={loginMutation.isPending} disabled={loginMutation.isPending}>
            Đăng nhập
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          <Link to="/" className="font-medium text-primary-600 hover:text-primary-700">
            Về trang chủ
          </Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
