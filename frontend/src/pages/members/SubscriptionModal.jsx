import { useEffect, useMemo } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Modal from '../../components/common/Modal'
import Select from '../../components/common/Select'
import Input from '../../components/common/Input'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { usePackagesQuery } from '../../hooks/usePackages'
import { useRegisterSubscription } from '../../hooks/useMembers'
import { formatCurrency } from '../../utils/formatters'

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

  const onSubmit = (values) => {
    registerMutation.mutate(
      {
        memberId: member.id,
        payload: {
          membershipId: Number(values.membershipId),
          startDate: values.startDate,
          notes: values.notes,
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

          <Textarea label="Ghi chú" placeholder="Đăng ký tại quầy lễ tân" rows={2} {...register('notes')} />

          <div className="rounded-lg bg-gray-50 p-4">
            <div className="flex items-center justify-between text-sm text-gray-600">
              <span>Đơn giá gói tập</span>
              <span>{selectedPackage ? formatCurrency(selectedPackage.price) : '—'}</span>
            </div>
            <div className="mt-2 flex items-center justify-between text-base font-bold text-gray-900">
              <span>Tổng số tiền cần thanh toán</span>
              <span className="text-primary-600">{selectedPackage ? formatCurrency(selectedPackage.price) : formatCurrency(0)}</span>
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
