import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useEffect } from 'react'
import Input from '../common/Input'
import Select from '../common/Select'
import Button from '../common/Button'
import { STAFF_ROLE, STAFF_ROLE_LABEL } from '../../utils/constants'
import { User, Mail, Phone, ShieldCheck, Key } from 'lucide-react'

const ROLE_OPTIONS = Object.values(STAFF_ROLE).map((value) => ({
  value,
  label: STAFF_ROLE_LABEL[value] || value,
}))

const schema = yup.object({
  name: yup.string().trim().required('Vui lòng nhập họ và tên'),
  email: yup
    .string()
    .trim()
    .email('Email không đúng định dạng')
    .required('Vui lòng nhập địa chỉ email'),
  phone: yup
    .string()
    .trim()
    .matches(
      /^(0[3|5|7|8|9])[0-9]{8}$/,
      'Số điện thoại không hợp lệ (gồm 10 số, bắt đầu bằng 03, 05, 07, 08, 09)'
    )
    .required('Vui lòng nhập số điện thoại'),
  role: yup.string().required('Vui lòng chọn vai trò'),
})

function StaffForm({ staff, onSubmit, onCancel, submitting }) {
  const isEdit = Boolean(staff)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      name: staff?.name || '',
      email: staff?.email || '',
      phone: staff?.phone || '',
      role: staff?.role || STAFF_ROLE.STAFF,
    },
  })

  useEffect(() => {
    reset({
      name: staff?.name || '',
      email: staff?.email || '',
      phone: staff?.phone || '',
      role: staff?.role || STAFF_ROLE.STAFF,
    })
  }, [staff, reset])

  const handleFormSubmit = (data) => {
    onSubmit(data)
  }

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
      <Input
        label="Họ và tên"
        placeholder="Ví dụ: Nguyễn Văn An"
        icon={User}
        error={errors.name?.message}
        {...register('name')}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          label="Địa chỉ email"
          type="email"
          placeholder="staff@gym.com"
          icon={Mail}
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Số điện thoại"
          type="tel"
          placeholder="0912345678"
          icon={Phone}
          error={errors.phone?.message}
          {...register('phone')}
        />
      </div>

      <Select
        label="Vai trò nhân sự"
        options={ROLE_OPTIONS}
        error={errors.role?.message}
        {...register('role')}
      />

      {!isEdit && (
        <div className="rounded-lg border border-blue-100 bg-blue-50/70 p-3 text-xs text-blue-800">
          <div className="flex items-center gap-1.5 font-semibold">
            <Key size={14} className="text-blue-600" />
            Mật khẩu khởi tạo
          </div>
          <p className="mt-1 text-blue-700">
            Mật khẩu mặc định là <span className="font-mono font-bold">123456</span>. Nhân viên có thể đổi mật khẩu sau khi đăng nhập lần đầu.
          </p>
        </div>
      )}

      {isEdit && (
        <div className="rounded-lg border border-gray-100 bg-gray-50 p-3 text-xs text-gray-600">
          <div className="flex items-center gap-1.5 font-semibold text-gray-700">
            <ShieldCheck size={14} className="text-primary-600" />
            Thông tin hệ thống
          </div>
          <p className="mt-1">
            Mã nhân viên: <span className="font-mono font-semibold">{staff?.id}</span> · Trạng thái:{' '}
            <span className="font-semibold">
              {staff?.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}
            </span>
          </p>
        </div>
      )}

      <div className="flex items-center justify-end gap-3 border-t border-gray-100 pt-4">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={submitting}>
          Hủy bỏ
        </Button>
        <Button type="submit" loading={submitting}>
          {isEdit ? 'Lưu thay đổi' : 'Thêm nhân viên'}
        </Button>
      </div>
    </form>
  )
}

export default StaffForm
