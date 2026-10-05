import { useEffect } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { useImportStock } from '../../hooks/useProducts'

// Ô number trống -> NaN; chuyển thành undefined để trường tùy chọn không báo lỗi kiểu dữ liệu
const emptyToUndefined = (value, originalValue) => (originalValue === '' ? undefined : value)

const schema = yup.object({
  productId: yup.string().required('Vui lòng chọn sản phẩm'),
  // Backend (StockImportDto) yêu cầu @Positive nên chặn luôn 0 để tránh lỗi 400
  quantity: yup
    .number()
    .typeError('Số lượng nhập không được để trống')
    .required('Số lượng nhập không được để trống')
    .integer('Số lượng nhập phải là số nguyên')
    .min(1, 'Số lượng nhập phải lớn hơn 0'),
  cost: yup
    .number()
    .transform(emptyToUndefined)
    .typeError('Giá nhập phải là số')
    .integer('Giá nhập phải là số nguyên')
    .min(0, 'Giá nhập không được âm'),
  note: yup.string().trim().max(255, 'Ghi chú tối đa 255 ký tự'),
})

const EMPTY_FORM = { productId: '', quantity: '', cost: '', note: '' }

function ImportStockModal({ open, onClose, products = [], product }) {
  const importMutation = useImportStock()
  const isSubmitting = importMutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema), defaultValues: EMPTY_FORM })

  useEffect(() => {
    if (open) reset({ ...EMPTY_FORM, productId: product ? String(product.id) : '' })
  }, [open, product, reset])

  const selectedId = useWatch({ control, name: 'productId' })
  const selected = products.find((p) => String(p.id) === selectedId)
  const quantity = Number(useWatch({ control, name: 'quantity' }))
  const previewStock = selected && Number.isInteger(quantity) && quantity > 0 ? selected.stock + quantity : null

  const handleClose = () => {
    if (isSubmitting) return
    reset(EMPTY_FORM)
    onClose()
  }

  const onSubmit = (values) => {
    const payload = { quantity: values.quantity }
    if (values.cost !== undefined) payload.cost = values.cost
    if (values.note) payload.note = values.note
    importMutation.mutate({ id: values.productId, payload }, { onSuccess: handleClose })
  }

  const productOptions = products.map((p) => ({ value: String(p.id), label: `${p.sku} — ${p.name}` }))

  return (
    <Modal open={open} onClose={handleClose} title="Nhập hàng vào kho">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          <Select
            label="Sản phẩm *"
            placeholder="Chọn sản phẩm"
            options={productOptions}
            error={errors.productId?.message}
            {...register('productId')}
          />

          {selected && (
            <div className="flex items-center justify-between rounded-lg bg-gray-50 px-4 py-3 text-sm">
              <span className="text-gray-500">
                Tồn kho hiện tại: <span className="font-semibold text-gray-900">{selected.stock}</span>
              </span>
              {previewStock !== null && (
                <span className="text-gray-500">
                  Sau khi nhập: <span className="font-semibold text-green-700">{previewStock}</span>
                </span>
              )}
            </div>
          )}

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Số lượng nhập thêm *"
              type="number"
              min="1"
              step="1"
              placeholder="20"
              error={errors.quantity?.message}
              {...register('quantity')}
            />
            <Input
              label="Giá nhập (VNĐ)"
              type="number"
              min="0"
              step="1"
              placeholder={selected ? String(selected.cost ?? '') : '500000'}
              error={errors.cost?.message}
              {...register('cost')}
            />
          </div>

          <Textarea
            label="Ghi chú / Nhà cung cấp"
            rows={3}
            placeholder="Nhập lô hàng tháng 10 - NCC ABC"
            error={errors.note?.message}
            {...register('note')}
          />
          <p className="text-xs text-gray-400">Để trống giá nhập nếu không thay đổi giá vốn hiện tại.</p>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={handleClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Xác nhận nhập kho
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default ImportStockModal
