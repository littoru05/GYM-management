import { useEffect, useMemo, useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { TicketPercent, CheckCircle2, XCircle, X } from 'lucide-react'
import Modal from '../../components/common/Modal'
import Select from '../../components/common/Select'
import Input from '../../components/common/Input'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { usePackagesQuery } from '../../hooks/usePackages'
import { useRegisterSubscription } from '../../hooks/useMembers'
import { formatCurrency } from '../../utils/formatters'
import * as voucherService from '../../services/voucherService'
import toast from 'react-hot-toast'

const schema = yup.object({
  membershipId: yup.string().required('Vui lòng chọn gói tập'),
  startDate: yup.string().required('Vui lòng chọn ngày bắt đầu'),
  notes: yup.string().nullable().transform((v) => v || null),
})

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

function SubscriptionModal({ open, onClose, member }) {
  const { data: packages, isLoading: isLoadingPackages } = usePackagesQuery({ status: 'ACTIVE' })
  const registerMutation = useRegisterSubscription()

  const [voucherCodeInput, setVoucherCodeInput] = useState('')
  const [appliedVoucher, setAppliedVoucher] = useState(null)
  const [voucherError, setVoucherError] = useState('')
  const [isValidatingVoucher, setIsValidatingVoucher] = useState(false)

  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { membershipId: '', startDate: todayIso(), notes: '' },
  })

  useEffect(() => {
    if (open) {
      reset({ membershipId: '', startDate: todayIso(), notes: '' })
      setVoucherCodeInput('')
      setAppliedVoucher(null)
      setVoucherError('')
    }
  }, [open, reset])

  const selectedMembershipId = watch('membershipId')
  const selectedPackage = useMemo(
    () => packages?.find((p) => String(p.id) === String(selectedMembershipId)),
    [packages, selectedMembershipId]
  )

  const packageOptions = useMemo(
    () => (packages || []).map((p) => ({ value: String(p.id), label: `${p.name} (${p.durationMonths} tháng)` })),
    [packages]
  )

  // Tự động kiểm tra lại voucher khi người dùng đổi gói tập
  useEffect(() => {
    if (!appliedVoucher || !selectedPackage) return

    let isMounted = true
    voucherService
      .validateVoucher({
        code: appliedVoucher.code,
        membershipId: selectedPackage.id,
        orderAmount: selectedPackage.price,
        memberId: member?.id,
      })
      .then((res) => {
        if (isMounted) setAppliedVoucher(res)
      })
      .catch((err) => {
        if (isMounted) {
          setAppliedVoucher(null)
          setVoucherError(`Voucher không áp dụng được cho gói mới: ${err.message}`)
        }
      })

    return () => {
      isMounted = false
    }
  }, [selectedPackage, member?.id])

  // Xử lý áp dụng voucher
  const handleApplyVoucher = async () => {
    const code = voucherCodeInput.trim()
    if (!code) {
      setVoucherError('Vui lòng nhập mã voucher')
      return
    }
    if (!selectedPackage) {
      setVoucherError('Vui lòng chọn gói tập trước khi áp dụng voucher')
      return
    }

    setIsValidatingVoucher(true)
    setVoucherError('')

    try {
      const result = await voucherService.validateVoucher({
        code,
        membershipId: selectedPackage.id,
        orderAmount: selectedPackage.price,
        memberId: member?.id,
      })
      setAppliedVoucher(result)
      setVoucherError('')
      toast.success(`Đã áp dụng mã ${result.code}: Giảm ${formatCurrency(result.discountAmount)}`)
    } catch (err) {
      setAppliedVoucher(null)
      setVoucherError(err.message || 'Mã voucher không hợp lệ')
    } finally {
      setIsValidatingVoucher(false)
    }
  }

  // Xử lý gỡ bỏ voucher
  const handleRemoveVoucher = () => {
    setAppliedVoucher(null)
    setVoucherCodeInput('')
    setVoucherError('')
  }

  // Tính toán số tiền thanh toán cuối cùng
  const finalPrice = useMemo(() => {
    if (!selectedPackage) return 0
    if (appliedVoucher) {
      return appliedVoucher.finalPrice ?? appliedVoucher.finalAmount ?? (selectedPackage.price - appliedVoucher.discountAmount)
    }
    return selectedPackage.price
  }, [selectedPackage, appliedVoucher])

  const onSubmit = (values) => {
    registerMutation.mutate(
      {
        memberId: member.id,
        payload: {
          membershipId: Number(values.membershipId),
          startDate: values.startDate,
          notes: values.notes,
          voucherCode: appliedVoucher ? appliedVoucher.code : undefined,
          paidPrice: finalPrice,
        },
      },
      { onSuccess: onClose }
    )
  }

  if (!member) return null

  return (
    <Modal open={open} onClose={onClose} title={`Đăng ký gói tập cho ${member.name}`}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          <p className="text-sm text-gray-500">
            Hội viên: <span className="font-medium text-gray-800">{member.name}</span> · Mã: {member.code}
          </p>

          <Controller
            control={control}
            name="membershipId"
            render={({ field }) => (
              <Select
                label="Gói tập *"
                placeholder={isLoadingPackages ? 'Đang tải danh sách gói...' : 'Chọn gói tập'}
                options={packageOptions}
                error={errors.membershipId?.message}
                {...field}
              />
            )}
          />

          <Input label="Ngày bắt đầu kích hoạt *" type="date" error={errors.startDate?.message} {...register('startDate')} />

          {/* Ô nhập mã Voucher */}
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Mã voucher giảm giá</label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                  <TicketPercent className="h-4 w-4 text-gray-400" />
                </div>
                <input
                  type="text"
                  placeholder="Nhập mã voucher (ví dụ: WELCOME10, SAVE50K)"
                  value={voucherCodeInput}
                  onChange={(e) => {
                    setVoucherCodeInput(e.target.value.toUpperCase())
                    if (voucherError) setVoucherError('')
                  }}
                  disabled={!!appliedVoucher || isValidatingVoucher}
                  className="block w-full rounded-lg border border-gray-300 py-2 pl-9 pr-3 text-sm uppercase placeholder:normal-case focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 disabled:bg-gray-100 disabled:text-gray-500"
                />
              </div>

              {appliedVoucher ? (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleRemoveVoucher}
                  icon={X}
                  className="border-red-200 text-red-600 hover:bg-red-50"
                >
                  Gỡ bỏ
                </Button>
              ) : (
                <Button
                  type="button"
                  variant="secondary"
                  onClick={handleApplyVoucher}
                  loading={isValidatingVoucher}
                  disabled={isValidatingVoucher || !voucherCodeInput.trim() || !selectedPackage}
                >
                  Áp dụng
                </Button>
              )}
            </div>

            {/* Thông báo voucher hợp lệ */}
            {appliedVoucher && (
              <div className="mt-2 flex items-center gap-1.5 text-xs font-medium text-emerald-600">
                <CheckCircle2 className="h-3.5 w-3.5 flex-shrink-0" />
                <span>
                  Đã áp dụng mã <strong className="font-semibold">{appliedVoucher.code}</strong>:{' '}
                  {appliedVoucher.name ? `${appliedVoucher.name} - ` : ''}giảm {formatCurrency(appliedVoucher.discountAmount)}
                </span>
              </div>
            )}

            {/* Thông báo lỗi voucher */}
            {voucherError && (
              <div className="mt-2 flex items-center gap-1.5 text-xs text-red-600">
                <XCircle className="h-3.5 w-3.5 flex-shrink-0" />
                <span>{voucherError}</span>
              </div>
            )}
          </div>

          <Textarea label="Ghi chú" placeholder="Đăng ký tại quầy lễ tân" rows={2} {...register('notes')} />

          {/* Bảng tổng kết số tiền */}
          <div className="rounded-lg bg-gray-50 p-4">
            <div className="flex items-center justify-between text-sm text-gray-600">
              <span>Đơn giá gói tập</span>
              <span>{selectedPackage ? formatCurrency(selectedPackage.price) : '—'}</span>
            </div>

            {appliedVoucher && (
              <div className="mt-1 flex items-center justify-between text-sm text-emerald-600">
                <span>Giảm giá ({appliedVoucher.code})</span>
                <span className="font-medium">-{formatCurrency(appliedVoucher.discountAmount)}</span>
              </div>
            )}

            <div className="mt-3 border-t border-gray-200 pt-2 flex items-center justify-between text-base font-bold text-gray-900">
              <span>Tổng số tiền cần thanh toán</span>
              <span className="text-primary-600">{formatCurrency(finalPrice)}</span>
            </div>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={registerMutation.isPending}>
            Hủy
          </Button>
          <Button type="submit" loading={registerMutation.isPending} disabled={registerMutation.isPending}>
            Xác nhận đăng ký
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default SubscriptionModal
