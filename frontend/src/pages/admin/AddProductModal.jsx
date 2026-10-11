import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Button from '../../components/common/Button'
import { useCreateProduct } from '../../hooks/useProducts'
import { productSchema, EMPTY_PRODUCT } from './productSchema'

function AddProductModal({ open, onClose, categories = [] }) {
  const createMutation = useCreateProduct()
  const isSubmitting = createMutation.isPending

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm({ resolver: yupResolver(productSchema), defaultValues: EMPTY_PRODUCT })

  // Mỗi lần mở modal đều bắt đầu với form trống
  useEffect(() => {
    if (open) reset(EMPTY_PRODUCT)
  }, [open, reset])

  const handleClose = () => {
    if (isSubmitting) return
    reset(EMPTY_PRODUCT)
    createMutation.reset()
    onClose()
  }

  const onSubmit = (values) => {
    createMutation.mutate(values, {
      onSuccess: handleClose,
      onError: (error) => {
        // Toast đỏ đã hiển thị trong hook; trùng SKU thì đánh dấu thêm lỗi ngay tại ô SKU
        if (error.response?.status === 409) {
          setError('sku', { type: 'server', message: error.response.data.message }, { shouldFocus: true })
        }
      },
    })
  }

  // Backend ProductDto.category nhận mã danh mục (code) và tự tra ra entity
  const categoryOptions = categories.map((c) => ({ value: c.code, label: c.name }))

  return (
    <Modal open={open} onClose={handleClose} title="Thêm sản phẩm mới" size="lg">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input label="Mã SKU *" placeholder="SUP-WHEY-002" error={errors.sku?.message} {...register('sku')} />
            <Select
              label="Danh mục *"
              placeholder="Chọn danh mục"
              options={categoryOptions}
              error={errors.category?.message}
              {...register('category')}
            />
          </div>

          <Input
            label="Tên sản phẩm *"
            placeholder="Whey Protein Isolate 2kg"
            error={errors.name?.message}
            {...register('name')}
          />

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Input label="Đơn vị tính *" placeholder="Hộp, Chai, Cái..." error={errors.unit?.message} {...register('unit')} />
            <Input
              label="Giá bán niêm yết (VNĐ) *"
              type="number"
              min="1"
              step="1"
              placeholder="750000"
              error={errors.price?.message}
              {...register('price')}
            />
            <Input
              label="Giá vốn (VNĐ) *"
              type="number"
              min="1"
              step="1"
              placeholder="500000"
              error={errors.cost?.message}
              {...register('cost')}
            />
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Số lượng nhập ban đầu *"
              type="number"
              min="1"
              step="1"
              placeholder="50"
              error={errors.stock?.message}
              {...register('stock')}
            />
            <Input
              label="Ngưỡng cảnh báo tối thiểu *"
              type="number"
              min="1"
              step="1"
              placeholder="10"
              error={errors.minStock?.message}
              {...register('minStock')}
            />
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={handleClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Thêm sản phẩm
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default AddProductModal
