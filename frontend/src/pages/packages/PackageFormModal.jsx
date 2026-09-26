import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Textarea from '../../components/common/Textarea'
import Select from '../../components/common/Select'
import Button from '../../components/common/Button'
import { useCreatePackage, useUpdatePackage } from '../../hooks/usePackages'

const schema = yup.object({
  name: yup.string().trim().required('Tên gói tập không được để trống').max(100, 'Tên gói tập tối đa 100 ký tự'),
  durationMonths: yup
    .number()
    .typeError('Thời hạn phải là số')
    .required('Thời hạn không được để trống')
    .integer('Thời hạn phải là số nguyên')
    .min(1, 'Thời hạn phải lớn hơn 0'),
  price: yup
    .number()
    .typeError('Đơn giá phải là số')
    .required('Đơn giá không được để trống')
    .min(0, 'Đơn giá không được âm'),
  description: yup.string().nullable().transform((v) => v || null),
  status: yup.string().required(),
})

const STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Đang bán' },
  { value: 'INACTIVE', label: 'Tạm ngưng' },
]

function PackageFormModal({ open, onClose, pkg }) {
  const isEdit = Boolean(pkg)
  const createMutation = useCreatePackage()
  const updateMutation = useUpdatePackage()
  const isSubmitting = createMutation.isPending || updateMutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema) })

  useEffect(() => {
    if (open) {
      reset({
        name: pkg?.name || '',
        durationMonths: pkg?.durationMonths ?? '',
        price: pkg?.price ?? '',
        description: pkg?.description || '',
        status: pkg?.status || 'ACTIVE',
      })
    }
  }, [open, pkg, reset])

  const onSubmit = (values) => {
    const payload = {
      name: values.name,
      durationMonths: values.durationMonths,
      price: values.price,
      description: values.description,
      status: values.status,
    }
    if (isEdit) {
      updateMutation.mutate({ id: pkg.id, payload }, { onSuccess: onClose })
    } else {
      createMutation.mutate(payload, { onSuccess: onClose })
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={isEdit ? 'Chỉnh sửa gói tập' : 'Thêm gói tập mới'}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          <Input label="Tên gói tập *" placeholder="Gói Gold 12 tháng" error={errors.name?.message} {...register('name')} />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Thời hạn (tháng) *"
              type="number"
              min="1"
              step="1"
              placeholder="1"
              error={errors.durationMonths?.message}
              {...register('durationMonths')}
            />
            <Input
              label="Đơn giá (VNĐ) *"
              type="number"
              min="0"
              step="1000"
              placeholder="500000"
              error={errors.price?.message}
              {...register('price')}
            />
          </div>

          <Select label="Trạng thái kinh doanh" options={STATUS_OPTIONS} error={errors.status?.message} {...register('status')} />

          <Textarea
            label="Mô tả quyền lợi"
            placeholder="Tập gym không giới hạn, hỗ trợ PT 2 buổi/tháng..."
            rows={3}
            error={errors.description?.message}
            {...register('description')}
          />
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting} disabled={isSubmitting}>
            {isEdit ? 'Lưu thay đổi' : 'Thêm gói tập'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default PackageFormModal
