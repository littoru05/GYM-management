import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import {
  Tag,
  Percent,
  Banknote,
  Calendar,
  Hash,
  Sparkles,
  TicketPercent,
  CheckCircle2,
} from 'lucide-react'
import Drawer from '../common/Drawer'
import Button from '../common/Button'
import { DISCOUNT_TYPE } from '../../utils/constants'
import { formatCurrency, formatDate } from '../../utils/formatters'

const schema = yup.object().shape({
  code: yup
    .string()
    .trim()
    .required('Vui lòng nhập mã voucher')
    .min(3, 'Mã voucher phải có ít nhất 3 ký tự')
    .max(20, 'Mã voucher tối đa 20 ký tự')
    .matches(/^[A-Za-z0-9_-]+$/, 'Mã chỉ được gồm chữ hoa, chữ số, gạch dưới hoặc gạch nối (-)'),
  name: yup
    .string()
    .trim()
    .required('Vui lòng nhập tên chương trình khuyến mãi')
    .min(3, 'Tên chương trình phải có ít nhất 3 ký tự')
    .max(100, 'Tên chương trình tối đa 100 ký tự'),
  description: yup.string().max(255, 'Mô tả tối đa 255 ký tự').nullable(),
  discountType: yup
    .string()
    .required('Vui lòng chọn loại giảm giá')
    .oneOf([DISCOUNT_TYPE.PERCENTAGE, DISCOUNT_TYPE.FIXED], 'Loại giảm giá không hợp lệ'),
  discountValue: yup
    .number()
    .typeError('Vui lòng nhập giá trị giảm là số')
    .required('Vui lòng nhập giá trị giảm')
    .positive('Giá trị giảm phải lớn hơn 0')
    .test('discount-range', function (val) {
      const { discountType } = this.parent
      if (discountType === DISCOUNT_TYPE.PERCENTAGE) {
        if (val < 1 || val > 100) {
          return this.createError({
            message: 'Mức giảm theo phần trăm phải từ 1% đến 100%',
          })
        }
      } else if (discountType === DISCOUNT_TYPE.FIXED) {
        if (val < 1000) {
          return this.createError({
            message: 'Số tiền giảm tối thiểu là 1.000 ₫',
          })
        }
      }
      return true
    }),
  startDate: yup.string().required('Vui lòng chọn ngày bắt đầu'),
  endDate: yup
    .string()
    .required('Vui lòng chọn ngày kết thúc')
    .test('end-after-start', 'Ngày kết thúc phải cùng ngày hoặc sau ngày bắt đầu', function (val) {
      const { startDate } = this.parent
      if (!startDate || !val) return true
      return new Date(val) >= new Date(startDate)
    }),
  usageLimit: yup
    .number()
    .typeError('Vui lòng nhập số lượt dùng tối đa')
    .required('Vui lòng nhập số lượt dùng tối đa')
    .positive('Số lượt dùng phải lớn hơn 0')
    .integer('Số lượt dùng phải là số nguyên')
    .min(1, 'Số lượt dùng tối thiểu là 1 lượt'),
  status: yup.string().default('ACTIVE'),
})

const DEFAULT_START_DATE = () => new Date().toISOString().slice(0, 10)
const DEFAULT_END_DATE = () => {
  const d = new Date()
  d.setDate(d.getDate() + 30)
  return d.toISOString().slice(0, 10)
}

function generateRandomCode() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = 'FIT'
  for (let i = 0; i < 5; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

function VoucherDrawer({ open, onClose, voucher, onSubmit, submitting }) {
  const isEditing = Boolean(voucher?.id)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    mode: 'onChange',
    defaultValues: {
      code: '',
      name: '',
      description: '',
      discountType: DISCOUNT_TYPE.PERCENTAGE,
      discountValue: '',
      startDate: DEFAULT_START_DATE(),
      endDate: DEFAULT_END_DATE(),
      usageLimit: 100,
      status: 'ACTIVE',
    },
  })

  // Sync values on voucher open
  useEffect(() => {
    if (open) {
      if (voucher) {
        reset({
          code: voucher.code || '',
          name: voucher.name || '',
          description: voucher.description || '',
          discountType: voucher.discountType || DISCOUNT_TYPE.PERCENTAGE,
          discountValue: voucher.discountValue ?? '',
          startDate: voucher.startDate || DEFAULT_START_DATE(),
          endDate: voucher.endDate || DEFAULT_END_DATE(),
          usageLimit: voucher.usageLimit ?? 100,
          status: voucher.status || 'ACTIVE',
        })
      } else {
        reset({
          code: '',
          name: '',
          description: '',
          discountType: DISCOUNT_TYPE.PERCENTAGE,
          discountValue: '',
          startDate: DEFAULT_START_DATE(),
          endDate: DEFAULT_END_DATE(),
          usageLimit: 100,
          status: 'ACTIVE',
        })
      }
    }
  }, [open, voucher, reset])

  const watchedValues = watch()
  const currentDiscountType = watchedValues.discountType || DISCOUNT_TYPE.PERCENTAGE
  const currentDiscountValue = Number(watchedValues.discountValue) || 0
  const currentStartDate = watchedValues.startDate
  const currentCode = (watchedValues.code || '').toUpperCase()

  // Preset quick date extension
  const applyDatePreset = (days) => {
    const start = currentStartDate ? new Date(currentStartDate) : new Date()
    start.setDate(start.getDate() + days)
    setValue('endDate', start.toISOString().slice(0, 10), { shouldValidate: true })
  }

  // Handle submit form
  const handleFormSubmit = (data) => {
    onSubmit({
      ...data,
      code: data.code.trim().toUpperCase(),
      discountValue: Number(data.discountValue),
      usageLimit: Number(data.usageLimit),
      description: data.description ? data.description.trim() : '',
    })
  }

  return (
    <Drawer
      open={open}
      onClose={onClose}
      size="xl"
      title={isEditing ? 'Chỉnh sửa voucher' : 'Thêm mới voucher'}
      subtitle={
        isEditing
          ? `Cập nhật thông tin chi tiết của voucher ${voucher?.code}`
          : 'Thiết lập mã ưu đãi, mức giảm giá và số lượt sử dụng'
      }
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
        {/* Ticket Live Preview Card */}
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary-600 via-primary-500 to-amber-500 p-5 text-white shadow-lg">
          <div className="absolute -right-8 -top-8 h-28 w-28 rounded-full bg-white/10 blur-xl pointer-events-none" />
          <div className="absolute -bottom-8 -left-8 h-28 w-28 rounded-full bg-amber-400/20 blur-xl pointer-events-none" />

          <div className="flex items-center justify-between border-b border-white/20 pb-3">
            <div className="flex items-center gap-2">
              <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-white/20 backdrop-blur-xs">
                <TicketPercent size={16} />
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-white/90">
                PowerFit Voucher Preview
              </span>
            </div>
            <span
              className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                watchedValues.status === 'ACTIVE'
                  ? 'bg-emerald-400/30 text-emerald-100 border border-emerald-300/40'
                  : 'bg-white/20 text-white/70'
              }`}
            >
              {watchedValues.status === 'ACTIVE' ? 'Sẵn sàng dùng' : 'Tạm dừng'}
            </span>
          </div>

          <div className="mt-4 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
            <div>
              <p className="text-xs font-medium text-white/80">
                {watchedValues.name || 'Tên chương trình khuyến mãi'}
              </p>
              <div className="mt-1 flex items-baseline gap-1.5">
                <span className="text-3xl font-extrabold tracking-tight">
                  {currentDiscountType === DISCOUNT_TYPE.PERCENTAGE
                    ? currentDiscountValue
                      ? `-${currentDiscountValue}%`
                      : '-%'
                    : currentDiscountValue
                    ? `-${formatCurrency(currentDiscountValue)}`
                    : '-0 ₫'}
                </span>
                <span className="text-xs text-white/80">
                  {currentDiscountType === DISCOUNT_TYPE.PERCENTAGE ? 'giảm trực tiếp' : 'trừ vào hóa đơn'}
                </span>
              </div>
            </div>

            <div className="flex flex-col items-start sm:items-end">
              <span className="text-[10px] uppercase tracking-wider text-white/70">Mã ưu đãi</span>
              <span className="font-mono text-base font-bold tracking-widest text-amber-200">
                {currentCode || 'MÃ-VOUCHER'}
              </span>
            </div>
          </div>

          <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-white/20 pt-3 text-[11px] text-white/85">
            <span>
              Hạn dùng:{' '}
              <strong className="text-white">
                {formatDate(watchedValues.startDate)} - {formatDate(watchedValues.endDate)}
              </strong>
            </span>
            <span>
              Lượt dùng:{' '}
              <strong className="text-white">
                {watchedValues.usageLimit ? `${watchedValues.usageLimit} lượt` : '—'}
              </strong>
            </span>
          </div>
        </div>

        {/* Form Section 1: Basic Information */}
        <div className="rounded-xl border border-gray-100 bg-gray-50/50 p-4 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500">
            Thông tin mã voucher
          </h3>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-sm font-medium text-gray-700">
                  Mã voucher <span className="text-red-500">*</span>
                </label>
                {!isEditing && (
                  <button
                    type="button"
                    onClick={() => setValue('code', generateRandomCode(), { shouldValidate: true })}
                    className="flex items-center gap-1 text-xs text-primary-600 hover:text-primary-700 font-medium"
                  >
                    <Sparkles size={12} /> Tạo ngẫu nhiên
                  </button>
                )}
              </div>
              <div className="relative">
                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Tag size={16} />
                </span>
                <input
                  type="text"
                  placeholder="VÍ DỤ: GYM2026, SUMMER10"
                  className={`w-full rounded-lg border bg-white pl-9 pr-3 py-2 text-sm font-mono font-semibold uppercase text-gray-900 placeholder:text-gray-400 placeholder:font-normal focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                    errors.code ? 'border-red-400' : 'border-gray-300'
                  }`}
                  {...register('code', {
                    onChange: (e) => {
                      e.target.value = e.target.value.toUpperCase()
                    },
                  })}
                />
              </div>
              {errors.code && <p className="mt-1 text-xs text-red-500">{errors.code.message}</p>}
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Tên khuyến mãi <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                placeholder="Ví dụ: Giảm 20% gói tập Hè"
                className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                  errors.name ? 'border-red-400' : 'border-gray-300'
                }`}
                {...register('name')}
              />
              {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
            </div>
          </div>

          <div>
            <label className="mb-1.5 block text-sm font-medium text-gray-700">
              Mô tả / Điều kiện áp dụng
            </label>
            <textarea
              rows={2}
              placeholder="Ghi chú thêm về điều kiện áp dụng cho voucher (tùy chọn)..."
              className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                errors.description ? 'border-red-400' : 'border-gray-300'
              }`}
              {...register('description')}
            />
            {errors.description && (
              <p className="mt-1 text-xs text-red-500">{errors.description.message}</p>
            )}
          </div>
        </div>

        {/* Form Section 2: Discount Configuration */}
        <div className="rounded-xl border border-gray-100 bg-gray-50/50 p-4 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500">
            Cấu hình mức giảm giá
          </h3>

          <div>
            <label className="mb-2 block text-sm font-medium text-gray-700">
              Loại giảm giá <span className="text-red-500">*</span>
            </label>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setValue('discountType', DISCOUNT_TYPE.PERCENTAGE, { shouldValidate: true })}
                className={`flex items-center justify-center gap-2 rounded-xl border p-3 text-sm font-medium transition-all ${
                  currentDiscountType === DISCOUNT_TYPE.PERCENTAGE
                    ? 'border-primary-500 bg-primary-50/70 text-primary-700 shadow-xs'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300'
                }`}
              >
                <div
                  className={`flex h-7 w-7 items-center justify-center rounded-lg ${
                    currentDiscountType === DISCOUNT_TYPE.PERCENTAGE
                      ? 'bg-primary-500 text-white'
                      : 'bg-gray-100 text-gray-500'
                  }`}
                >
                  <Percent size={15} />
                </div>
                <div className="text-left">
                  <p className="font-semibold leading-tight">Theo phần trăm (%)</p>
                  <span className="text-[11px] text-gray-500">Tối đa 100%</span>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setValue('discountType', DISCOUNT_TYPE.FIXED, { shouldValidate: true })}
                className={`flex items-center justify-center gap-2 rounded-xl border p-3 text-sm font-medium transition-all ${
                  currentDiscountType === DISCOUNT_TYPE.FIXED
                    ? 'border-primary-500 bg-primary-50/70 text-primary-700 shadow-xs'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300'
                }`}
              >
                <div
                  className={`flex h-7 w-7 items-center justify-center rounded-lg ${
                    currentDiscountType === DISCOUNT_TYPE.FIXED
                      ? 'bg-primary-500 text-white'
                      : 'bg-gray-100 text-gray-500'
                  }`}
                >
                  <Banknote size={15} />
                </div>
                <div className="text-left">
                  <p className="font-semibold leading-tight">Số tiền cố định (VNĐ)</p>
                  <span className="text-[11px] text-gray-500">Khấu trừ tiền mặt</span>
                </div>
              </button>
            </div>
            {errors.discountType && (
              <p className="mt-1 text-xs text-red-500">{errors.discountType.message}</p>
            )}
          </div>

          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-sm font-medium text-gray-700">
                Giá trị giảm <span className="text-red-500">*</span>
              </label>
              {currentDiscountType === DISCOUNT_TYPE.FIXED && currentDiscountValue > 0 && (
                <span className="text-xs font-semibold text-emerald-600">
                  Quy đổi: {formatCurrency(currentDiscountValue)}
                </span>
              )}
            </div>
            <div className="relative">
              <input
                type="number"
                step={currentDiscountType === DISCOUNT_TYPE.PERCENTAGE ? '1' : '1000'}
                placeholder={
                  currentDiscountType === DISCOUNT_TYPE.PERCENTAGE
                    ? 'Ví dụ: 10, 15, 20...'
                    : 'Ví dụ: 50000, 100000...'
                }
                className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                  currentDiscountType === DISCOUNT_TYPE.PERCENTAGE ? 'pr-9' : 'pr-12'
                } ${errors.discountValue ? 'border-red-400' : 'border-gray-300'}`}
                {...register('discountValue')}
              />
              <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 font-semibold text-gray-400">
                {currentDiscountType === DISCOUNT_TYPE.PERCENTAGE ? '%' : 'VNĐ'}
              </span>
            </div>
            {errors.discountValue && (
              <p className="mt-1 text-xs text-red-500">{errors.discountValue.message}</p>
            )}

            {/* Quick value helpers */}
            <div className="mt-2 flex flex-wrap items-center gap-1.5">
              <span className="text-[11px] text-gray-500">Gợi ý nhanh:</span>
              {currentDiscountType === DISCOUNT_TYPE.PERCENTAGE
                ? [5, 10, 15, 20, 30, 50].map((pct) => (
                    <button
                      key={pct}
                      type="button"
                      onClick={() => setValue('discountValue', pct, { shouldValidate: true })}
                      className="rounded-md border border-gray-200 bg-white px-2 py-0.5 text-xs font-medium text-gray-700 hover:bg-gray-100 hover:text-primary-600 transition-colors"
                    >
                      {pct}%
                    </button>
                  ))
                : [20000, 50000, 100000, 200000, 500000].map((val) => (
                    <button
                      key={val}
                      type="button"
                      onClick={() => setValue('discountValue', val, { shouldValidate: true })}
                      className="rounded-md border border-gray-200 bg-white px-2 py-0.5 text-xs font-medium text-gray-700 hover:bg-gray-100 hover:text-primary-600 transition-colors"
                    >
                      {formatCurrency(val)}
                    </button>
                  ))}
            </div>
          </div>
        </div>

        {/* Form Section 3: Validity Period & Usage Limit */}
        <div className="rounded-xl border border-gray-100 bg-gray-50/50 p-4 space-y-4">
          <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500">
            Thời hạn & Lượt sử dụng
          </h3>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Ngày bắt đầu <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Calendar size={16} />
                </span>
                <input
                  type="date"
                  className={`w-full rounded-lg border bg-white pl-9 pr-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                    errors.startDate ? 'border-red-400' : 'border-gray-300'
                  }`}
                  {...register('startDate')}
                />
              </div>
              {errors.startDate && (
                <p className="mt-1 text-xs text-red-500">{errors.startDate.message}</p>
              )}
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Ngày kết thúc <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Calendar size={16} />
                </span>
                <input
                  type="date"
                  className={`w-full rounded-lg border bg-white pl-9 pr-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                    errors.endDate ? 'border-red-400' : 'border-gray-300'
                  }`}
                  {...register('endDate')}
                />
              </div>
              {errors.endDate && (
                <p className="mt-1 text-xs text-red-500">{errors.endDate.message}</p>
              )}
            </div>
          </div>

          {/* Quick presets for validity duration */}
          <div className="flex flex-wrap items-center gap-1.5">
            <span className="text-[11px] text-gray-500">Thời hạn kết thúc nhanh:</span>
            {[
              { label: '+7 ngày', days: 7 },
              { label: '+15 ngày', days: 15 },
              { label: '+30 ngày', days: 30 },
              { label: '+60 ngày', days: 60 },
              { label: '+90 ngày', days: 90 },
              { label: '+1 năm', days: 365 },
            ].map((p) => (
              <button
                key={p.days}
                type="button"
                onClick={() => applyDatePreset(p.days)}
                className="rounded-md border border-gray-200 bg-white px-2 py-0.5 text-xs font-medium text-gray-700 hover:bg-gray-100 hover:text-primary-600 transition-colors"
              >
                {p.label}
              </button>
            ))}
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 pt-2">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Số lượt dùng tối đa <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Hash size={16} />
                </span>
                <input
                  type="number"
                  placeholder="Ví dụ: 50, 100, 500"
                  className={`w-full rounded-lg border bg-white pl-9 pr-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
                    errors.usageLimit ? 'border-red-400' : 'border-gray-300'
                  }`}
                  {...register('usageLimit')}
                />
              </div>
              {errors.usageLimit && (
                <p className="mt-1 text-xs text-red-500">{errors.usageLimit.message}</p>
              )}
              <div className="mt-1.5 flex gap-1">
                {[50, 100, 200, 500, 1000].map((limit) => (
                  <button
                    key={limit}
                    type="button"
                    onClick={() => setValue('usageLimit', limit, { shouldValidate: true })}
                    className="rounded border border-gray-200 bg-white px-1.5 py-0.5 text-[11px] text-gray-600 hover:bg-gray-100"
                  >
                    {limit}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700">
                Trạng thái ban đầu
              </label>
              <div className="flex h-10 items-center justify-between rounded-lg border border-gray-300 bg-white px-3">
                <span className="text-sm text-gray-700 font-medium">Kích hoạt voucher ngay</span>
                <input
                  type="checkbox"
                  checked={watchedValues.status === 'ACTIVE'}
                  onChange={(e) =>
                    setValue('status', e.target.checked ? 'ACTIVE' : 'INACTIVE', {
                      shouldValidate: true,
                    })
                  }
                  className="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="sticky bottom-0 -mx-6 -mb-5 flex items-center justify-end gap-3 border-t border-gray-100 bg-white px-6 py-4 shadow-lg">
          <Button type="button" variant="secondary" onClick={onClose} disabled={submitting}>
            Hủy bỏ
          </Button>
          <Button
            type="submit"
            icon={isEditing ? CheckCircle2 : Sparkles}
            loading={submitting}
          >
            {isEditing ? 'Lưu thay đổi' : 'Thêm voucher'}
          </Button>
        </div>
      </form>
    </Drawer>
  )
}

export default VoucherDrawer
