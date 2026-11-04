import { useEffect, useRef, useState } from 'react'
import { CheckCircle2, Phone, Mail, Sparkles, Send, Dumbbell } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Input from '../common/Input'
import Textarea from '../common/Textarea'
import Button from '../common/Button'
import { useSubmitContactLead } from '../../hooks/useContactLeads'
import { formatCurrency } from '../../utils/formatters'

const schema = yup.object({
  name: yup
    .string()
    .trim()
    .required('Họ và tên không được để trống')
    .min(2, 'Họ và tên phải từ 2 đến 50 ký tự')
    .max(50, 'Họ và tên phải từ 2 đến 50 ký tự')
    .matches(/^[a-zA-ZÀ-ỹ\s]+$/u, 'Họ và tên chỉ được chứa chữ cái, không được chứa số hoặc ký tự đặc biệt'),
  email: yup
    .string()
    .trim()
    .required('Email không được để trống')
    .matches(/^[a-zA-Z0-9._%+-]+@gmail\.com$/, 'Email phải đúng định dạng và có đuôi @gmail.com (ví dụ: yourname@gmail.com)'),
  phone: yup
    .string()
    .trim()
    .required('Số điện thoại không được để trống')
    .matches(/^(0[3|5|7|8|9])[0-9]{8}$/, 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09'),
  packageName: yup.string().nullable(),
  message: yup.string().trim().max(500, 'Nội dung tư vấn tối đa 500 ký tự'),
})

function PackageConsultationForm({
  selectedPackage: initialSelectedPackage = null,
  onSelectPackage,
  title = 'Đăng Ký Nhận Tư Vấn Gói Tập',
  subtitle = 'Để lại thông tin bên dưới, chuyên viên tư vấn PowerFit Gym sẽ liên hệ hỗ trợ bạn chọn gói tập phù hợp và nhận ưu đãi tốt nhất trong vòng 15 phút.',
}) {
  const [internalPackage, setInternalPackage] = useState(null)
  const currentPackage = initialSelectedPackage || internalPackage
  const submitMutation = useSubmitContactLead()
  const isSubmittingRef = useRef(false)

  const buildDefaultMessage = (pkg) => {
    if (!pkg) return 'Tôi muốn tìm hiểu thông tin và chính sách ưu đãi các gói tập tại PowerFit Gym.'
    return `Tôi muốn nhận tư vấn chi tiết về quyền lợi gói tập ${pkg.name} (${pkg.durationMonths} tháng - ${formatCurrency(
      pkg.price
    )}) bao gồm số buổi InBody, PT định hướng và phòng xông hơi Sauna.`
  }

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      packageName: initialSelectedPackage?.name || '',
      message: buildDefaultMessage(initialSelectedPackage),
    },
  })

  // Synchronize when initialSelectedPackage prop changes
  useEffect(() => {
    if (initialSelectedPackage) {
      setValue('packageName', initialSelectedPackage.name)
      setValue('message', buildDefaultMessage(initialSelectedPackage))
    }
  }, [initialSelectedPackage, setValue])

  // Listen to window custom event 'select-membership-package'
  useEffect(() => {
    const handleSelectEvent = (event) => {
      const pkg = event.detail
      if (pkg) {
        setInternalPackage(pkg)
        if (onSelectPackage) onSelectPackage(pkg)
        setValue('packageName', pkg.name)
        setValue('message', buildDefaultMessage(pkg))
      }
    }

    window.addEventListener('select-membership-package', handleSelectEvent)
    return () => window.removeEventListener('select-membership-package', handleSelectEvent)
  }, [onSelectPackage, setValue])

  const onSubmit = (values) => {
    // Chặn triệt để bấm gửi nhiều lần gây trùng lặp liên hệ
    if (submitMutation.isPending || isSubmittingRef.current) return
    isSubmittingRef.current = true

    const formattedPayload = {
      name: values.name,
      email: values.email,
      phone: values.phone,
      message: values.packageName
        ? `[Gói quan tâm: ${values.packageName}] ${values.message || ''}`.trim()
        : values.message,
    }

    submitMutation.mutate(formattedPayload, {
      onSuccess: () => {
        isSubmittingRef.current = false
        reset({
          name: '',
          email: '',
          phone: '',
          packageName: currentPackage?.name || '',
          message: buildDefaultMessage(currentPackage),
        })
      },
      onError: () => {
        isSubmittingRef.current = false
      },
    })
  }

  const handleFormSubmit = (e) => {
    e.preventDefault()
    if (submitMutation.isPending || isSubmittingRef.current) return
    handleSubmit(onSubmit)(e)
  }

  return (
    <section id="contact-form" className="scroll-mt-24 py-12">
      <div className="mx-auto max-w-4xl rounded-3xl border border-primary-100 bg-gradient-to-br from-white via-primary-50/20 to-white p-6 shadow-xl sm:p-10">
        <div className="text-center">
          <div className="inline-flex items-center gap-1.5 rounded-full bg-primary-100 px-3.5 py-1 text-xs font-bold uppercase tracking-wider text-primary-700">
            <Sparkles size={14} />
            <span>Tư vấn miễn phí 100%</span>
          </div>
          <h2 className="mt-3 text-2xl font-extrabold text-gray-900 sm:text-3xl">{title}</h2>
          <p className="mx-auto mt-2 max-w-2xl text-sm text-gray-600 sm:text-base">{subtitle}</p>
        </div>

        {/* Selected Package Banner Indicator */}
        {currentPackage && (
          <div className="mt-6 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-primary-200 bg-primary-50/80 p-4 transition-all">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-primary-500 text-white shadow-sm">
                <Dumbbell size={20} />
              </div>
              <div>
                <span className="text-[11px] font-bold uppercase tracking-wider text-primary-700">
                  Gói tập bạn đã chọn tư vấn
                </span>
                <p className="text-base font-bold text-gray-900 sm:text-lg">
                  {currentPackage.name} ({currentPackage.durationMonths} tháng) —{' '}
                  <span className="text-primary-600">{formatCurrency(currentPackage.price)}</span>
                </p>
                <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-gray-600">
                  <span>📊 InBody: {currentPackage.inbodySessions || '01 buổi'}</span>
                  <span>•</span>
                  <span>🧑‍🏫 PT: {currentPackage.ptSessions || '01 buổi'}</span>
                  <span>•</span>
                  <span>♨️ Sauna: {currentPackage.saunaLabel || (currentPackage.saunaAccess ? 'Có' : 'Chưa gồm')}</span>
                </div>
              </div>
            </div>

            <button
              type="button"
              onClick={() => {
                setInternalPackage(null)
                if (onSelectPackage) onSelectPackage(null)
                setValue('packageName', '')
                setValue('message', buildDefaultMessage(null))
              }}
              className="rounded-lg border border-primary-200 bg-white px-3 py-1.5 text-xs font-semibold text-primary-700 hover:bg-primary-50 transition-colors"
            >
              Chọn gói khác
            </button>
          </div>
        )}

        {/* Form Container */}
        <form onSubmit={handleFormSubmit} noValidate className="mt-8 space-y-5">
          <fieldset disabled={submitMutation.isPending} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Input
                label="Họ và tên"
                placeholder="Nguyễn Văn A"
                error={errors.name?.message}
                {...register('name')}
              />
              <Input
                label="Số điện thoại"
                placeholder="09xxxxxxxx"
                error={errors.phone?.message}
                {...register('phone')}
              />
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Input
                label="Email"
                type="email"
                placeholder="yourname@gmail.com"
                error={errors.email?.message}
                {...register('email')}
              />
              <div>
                <label className="mb-1.5 block text-xs font-semibold text-gray-700">
                  Gói tập quan tâm
                </label>
                <select
                  className="w-full rounded-xl border border-gray-200 bg-white px-3.5 py-2.5 text-sm text-gray-900 shadow-xs focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                  {...register('packageName')}
                  onChange={(e) => {
                    const val = e.target.value
                    setValue('packageName', val)
                    if (val) {
                      setValue(
                        'message',
                        `Tôi quan tâm đến gói tập ${val}. Vui lòng liên hệ tư vấn chi tiết quyền lợi và chính sách ưu đãi.`
                      )
                    }
                  }}
                >
                  <option value="">-- Chọn gói tập bạn muốn tư vấn --</option>
                  <option value="Basic (1 tháng)">Gói Basic (1 tháng - 500.000 đ)</option>
                  <option value="Standard (3 tháng)">Gói Standard (3 tháng - 1.350.000 đ)</option>
                  <option value="Premium (6 tháng)">Gói Premium (6 tháng - 2.400.000 đ) [Phổ biến]</option>
                  <option value="VIP / Platinum (12 tháng)">Gói VIP / Platinum (12 tháng - 4.200.000 đ) [Đẳng cấp]</option>
                </select>
              </div>
            </div>

            <Textarea
              label="Nội dung cần tư vấn"
              placeholder="Bạn có câu hỏi nào về lịch tập, huấn luyện viên hay cơ sở vật chất?"
              rows={3}
              error={errors.message?.message}
              {...register('message')}
            />

            <div className="pt-2">
              <Button
                type="submit"
                fullWidth
                size="lg"
                loading={submitMutation.isPending}
                disabled={submitMutation.isPending}
                icon={Send}
              >
                {submitMutation.isPending ? 'Đang gửi thông tin...' : 'Gửi yêu cầu nhận tư vấn gói tập'}
              </Button>
            </div>
          </fieldset>
        </form>

        <div className="mt-6 flex flex-wrap items-center justify-center gap-6 border-t border-gray-100 pt-5 text-xs text-gray-500">
          <span className="flex items-center gap-1.5">
            <CheckCircle2 size={14} className="text-emerald-500" />
            <span>Bảo mật thông tin 100%</span>
          </span>
          <span className="flex items-center gap-1.5">
            <Phone size={14} className="text-primary-500" />
            <span>Hotline hỗ trợ: <strong className="text-gray-700">1900 1234</strong></span>
          </span>
          <span className="flex items-center gap-1.5">
            <Mail size={14} className="text-primary-500" />
            <span>contact@powerfitgym.vn</span>
          </span>
        </div>
      </div>
    </section>
  )
}

export default PackageConsultationForm
