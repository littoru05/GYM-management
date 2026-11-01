import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { useCreateMember, useUpdateMember } from '../../hooks/useMembers'

const schema = yup.object({
  name: yup.string().trim().required('Họ tên không được để trống'),
  phone: yup
    .string()
    .trim()
    .required('Số điện thoại không được để trống')
    .matches(/^0[0-9]{9}$/, 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0'),
  email: yup.string().trim().email('Email không đúng định dạng').nullable().transform((v) => v || null),
  dob: yup.string().nullable().transform((v) => v || null),
  gender: yup.string().nullable().transform((v) => v || null),
  address: yup.string().nullable().transform((v) => v || null),
  healthNotes: yup.string().nullable().transform((v) => v || null),
})

const GENDER_OPTIONS = [
  { value: 'Nam', label: 'Nam' },
  { value: 'Nữ', label: 'Nữ' },
  { value: 'Khác', label: 'Khác' },
]

function MemberFormModal({ open, onClose, member }) {
  const isEdit = Boolean(member)
  const createMutation = useCreateMember()
  const updateMutation = useUpdateMember()
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
        name: member?.name || '',
        phone: member?.phone || '',
        email: member?.email || '',
        dob: member?.dob || '',
        gender: member?.gender || '',
        address: member?.address || '',
        healthNotes: member?.healthNotes || '',
      })
    }
  }, [open, member, reset])

  const onSubmit = (values) => {
    const payload = { ...values }
    if (isEdit) {
      updateMutation.mutate(
        { id: member.id, payload },
        { onSuccess: onClose }
      )
    } else {
      createMutation.mutate(payload, { onSuccess: onClose })
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={isEdit ? 'Cập nhật hội viên' : 'Thêm hội viên mới'} size="lg">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Họ và tên *"
            placeholder="Nguyễn Văn A"
            error={errors.name?.message}
            containerClassName="col-span-2"
            {...register('name')}
          />
          <Input
            label="Số điện thoại *"
            placeholder="0901234567"
            error={errors.phone?.message}
            {...register('phone')}
          />
          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input label="Ngày sinh" type="date" error={errors.dob?.message} {...register('dob')} />
          <Select
            label="Giới tính"
            placeholder="Chọn giới tính"
            options={GENDER_OPTIONS}
            error={errors.gender?.message}
            {...register('gender')}
          />
          <Input
            label="Địa chỉ"
            placeholder="123 Nguyễn Huệ, Q.1, TP.HCM"
            containerClassName="col-span-2"
            error={errors.address?.message}
            {...register('address')}
          />
          <Textarea
            label="Ghi chú sức khỏe"
            placeholder="Sức khỏe tốt, không có bệnh nền"
            rows={3}
            containerClassName="col-span-2"
            error={errors.healthNotes?.message}
            {...register('healthNotes')}
          />
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting} disabled={isSubmitting}>
            {isEdit ? 'Lưu thay đổi' : 'Thêm hội viên'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default MemberFormModal
