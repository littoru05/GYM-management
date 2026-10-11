import { useEffect } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { useAdjustStock } from '../../hooks/useProducts'

const REASON_OPTIONS = [
  { value: 'Hỏng hóc', label: 'Hỏng hóc' },
  { value: 'Thất thoát', label: 'Thất thoát' },
  { value: 'Bù trừ', label: 'Bù trừ (chênh lệch kiểm kê)' },
]

const schema = yup.object({
  actualStock: yup
    .number()
    .typeError('Số lượng kiểm kê không được để trống')
    .required('Số lượng kiểm kê không được để trống')
    .integer('Số lượng kiểm kê phải là số nguyên')
    .min(0, 'Số lượng kiểm kê không được âm'),
  reason: yup.string().required('Vui lòng chọn lý do điều chỉnh'),
  note: yup.string().trim().max(200, 'Ghi chú tối đa 200 ký tự'),
})

const EMPTY_FORM = { actualStock: '', reason: '', note: '' }

function AdjustStockModal({ open, onClose, product }) {
  const adjustMutation = useAdjustStock()
  const isSubmitting = adjustMutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema), defaultValues: EMPTY_FORM })

  useEffect(() => {
    if (open) reset(EMPTY_FORM)
  }, [open, reset])

  const actualStockInput = useWatch({ control, name: 'actualStock' })
  const actualStock = Number(actualStockInput)
  const hasValidActual = actualStockInput !== '' && Number.isInteger(actualStock) && actualStock >= 0
  const difference = hasValidActual && product ? actualStock - product.stock : null

  const handleClose = () => {
    if (isSubmitting) return
    reset(EMPTY_FORM)
    onClose()
  }

  const onSubmit = (values) => {
    // Backend chỉ có một trường `reason`, nên ghép lý do và ghi chú chi tiết
    const reason = values.note ? `${values.reason}: ${values.note}` : values.reason
    adjustMutation.mutate(
      { id: product.id, payload: { actualStock: values.actualStock, reason } },
      { onSuccess: handleClose }
    )
  }

  if (!product) return null

  return (
    <Modal open={open} onClose={handleClose} title="Điều chỉnh tồn kho sau kiểm kê">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          <div className="rounded-lg bg-gray-50 px-4 py-3 text-sm">
            <p className="font-medium text-gray-800">{product.name}</p>
            <p className="mt-0.5 font-mono text-xs text-gray-500">{product.sku}</p>
            <div className="mt-2 flex items-center justify-between text-gray-500">
              <span>
                Tồn kho hệ thống: <span className="font-semibold text-gray-900">{product.stock}</span>
              </span>
              {difference !== null && (
                <span>
                  Chênh lệch:{' '}
                  <span
                    className={`font-semibold ${
                      difference > 0 ? 'text-green-700' : difference < 0 ? 'text-red-700' : 'text-gray-900'
                    }`}
                  >
                    {difference > 0 ? `+${difference}` : difference}
                  </span>
                </span>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Số lượng kiểm kê thực tế *"
              type="number"
              min="0"
              step="1"
              placeholder={String(product.stock)}
              error={errors.actualStock?.message}
              {...register('actualStock')}
            />
            <Select
              label="Lý do điều chỉnh *"
              placeholder="Chọn lý do"
              options={REASON_OPTIONS}
              error={errors.reason?.message}
              {...register('reason')}
            />
          </div>

          <Textarea
            label="Ghi chú chi tiết"
            rows={3}
            placeholder="Rách bao bì 2 hộp khi vận chuyển..."
            error={errors.note?.message}
            {...register('note')}
          />
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={handleClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Xác nhận điều chỉnh
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default AdjustStockModal
