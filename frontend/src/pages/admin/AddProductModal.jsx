import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import { UploadCloud, Trash2, RefreshCw, AlertCircle, FileImage } from 'lucide-react'
import Modal from '../../components/common/Modal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Button from '../../components/common/Button'
import ImagePreview from '../../components/common/ImagePreview'
import { useCreateProduct, useUpdateProduct } from '../../hooks/useProducts'
import { uploadProductImage } from '../../services/inventoryService'
import { productSchema, EMPTY_PRODUCT } from './productSchema'

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
const ALLOWED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp', 'image/gif']

const STATUS_OPTIONS = [
  { value: 'ACTIVE', label: 'Đang kinh doanh' },
  { value: 'INACTIVE', label: 'Ngừng kinh doanh' },
]

function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`
}

function AddProductModal({ open, onClose, categories = [], product = null }) {
  const isEdit = Boolean(product && product.id)
  const createMutation = useCreateProduct()
  const updateMutation = useUpdateProduct()

  const [selectedFile, setSelectedFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [fileError, setFileError] = useState(null)
  const [isDragging, setIsDragging] = useState(false)
  const [isUploading, setIsUploading] = useState(false)

  const fileInputRef = useRef(null)
  const objectUrlRef = useRef(null)

  const isSubmitting = createMutation.isPending || updateMutation.isPending || isUploading

  const {
    register,
    handleSubmit,
    reset,
    setError,
    watch,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(productSchema),
    defaultValues: EMPTY_PRODUCT,
  })

  const currentProductName = watch('name')

  // Thu hồi Object URL tạm khi thay đổi file hoặc đóng modal
  const revokeCurrentObjectUrl = () => {
    if (objectUrlRef.current) {
      URL.revokeObjectURL(objectUrlRef.current)
      objectUrlRef.current = null
    }
  }

  // Đồng bộ dữ liệu khi mở modal (thêm mới hoặc chỉnh sửa)
  useEffect(() => {
    if (!open) return

    revokeCurrentObjectUrl()
    setSelectedFile(null)
    setFileError(null)
    setIsDragging(false)
    setIsUploading(false)

    if (product) {
      const initialImg = product.imageUrl || product.image || ''
      setPreviewUrl(initialImg)
      reset({
        sku: product.sku || '',
        name: product.name || '',
        category: product.category || '',
        unit: product.unit || 'Cái',
        price: product.price != null ? String(product.price) : '',
        cost: product.cost != null ? String(product.cost) : '',
        stock: product.stock != null ? String(product.stock) : '0',
        minStock: product.minStock != null ? String(product.minStock) : '10',
        status: product.status || 'ACTIVE',
        imageUrl: initialImg,
      })
    } else {
      setPreviewUrl('')
      reset(EMPTY_PRODUCT)
    }

    return () => {
      revokeCurrentObjectUrl()
    }
  }, [open, product, reset])

  const handleClose = () => {
    if (isSubmitting) return
    revokeCurrentObjectUrl()
    setSelectedFile(null)
    setPreviewUrl('')
    setFileError(null)
    reset(EMPTY_PRODUCT)
    createMutation.reset()
    updateMutation.reset()
    onClose()
  }

  // Xử lý khi người dùng chọn file từ thẻ input hoặc kéo thả
  const processSelectedFile = (file) => {
    if (!file) return

    if (!ALLOWED_IMAGE_TYPES.includes(file.type.toLowerCase())) {
      setFileError('Chỉ chấp nhận file định dạng hình ảnh (PNG, JPG, JPEG, WEBP, GIF).')
      return
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setFileError('Dung lượng file ảnh vượt quá 10MB. Vui lòng chọn ảnh nhỏ hơn.')
      return
    }

    setFileError(null)
    setSelectedFile(file)

    revokeCurrentObjectUrl()
    const objectUrl = URL.createObjectURL(file)
    objectUrlRef.current = objectUrl
    setPreviewUrl(objectUrl)
  }

  const handleFileInputChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      processSelectedFile(file)
    }
    // Reset value để có thể chọn lại cùng một file nếu vừa gỡ
    if (e.target) {
      e.target.value = ''
    }
  }

  const handleDragOver = (e) => {
    e.preventDefault()
    e.stopPropagation()
    if (!isSubmitting) {
      setIsDragging(true)
    }
  }

  const handleDragLeave = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
    if (isSubmitting) return

    const file = e.dataTransfer.files?.[0]
    if (file) {
      processSelectedFile(file)
    }
  }

  const handleRemoveImage = () => {
    revokeCurrentObjectUrl()
    setSelectedFile(null)
    setPreviewUrl('')
    setFileError(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const onSubmit = async (values) => {
    try {
      let finalImageUrl = previewUrl ? previewUrl.trim() : null

      // Nếu người dùng chọn file ảnh mới từ máy tính, upload lên server trước
      if (selectedFile) {
        setIsUploading(true)
        const uploadResult = await uploadProductImage(selectedFile)
        finalImageUrl = uploadResult.url || uploadResult.imageUrl || null
      }

      const payload = {
        ...values,
        imageUrl: finalImageUrl,
      }

      if (isEdit) {
        updateMutation.mutate(
          { id: product.id, payload },
          {
            onSuccess: handleClose,
            onError: (error) => {
              setIsUploading(false)
              if (error.response?.status === 409) {
                setError(
                  'sku',
                  { type: 'server', message: error.response?.data?.message || 'Mã SKU đã tồn tại trong hệ thống' },
                  { shouldFocus: true }
                )
              }
            },
          }
        )
      } else {
        createMutation.mutate(payload, {
          onSuccess: handleClose,
          onError: (error) => {
            setIsUploading(false)
            if (error.response?.status === 409) {
              setError(
                'sku',
                { type: 'server', message: error.response?.data?.message || 'Mã SKU đã tồn tại trong hệ thống' },
                { shouldFocus: true }
              )
            }
          },
        })
      }
    } catch (uploadErr) {
      setIsUploading(false)
      setFileError(uploadErr.response?.data?.message || uploadErr.message || 'Lỗi khi tải ảnh lên máy chủ.')
    }
  }

  // Backend ProductDto.category nhận mã danh mục (code) và tự tra ra entity
  const categoryOptions = categories.map((c) => ({ value: c.code, label: c.name }))

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title={isEdit ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}
      size="lg"
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <div className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label="Mã SKU *"
              placeholder="SUP-WHEY-002"
              error={errors.sku?.message}
              disabled={isEdit}
              {...register('sku')}
            />
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
            <Input
              label="Đơn vị tính *"
              placeholder="Hộp, Chai, Cái..."
              error={errors.unit?.message}
              {...register('unit')}
            />
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

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Input
              label="Số lượng tồn kho *"
              type="number"
              min="0"
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
            <Select
              label="Trạng thái kinh doanh *"
              options={STATUS_OPTIONS}
              error={errors.status?.message}
              {...register('status')}
            />
          </div>

          {/* Phần Chọn / Tải ảnh sản phẩm bằng thẻ input file */}
          <div className="space-y-3 pt-2 border-t border-gray-100">
            <label className="block text-sm font-semibold text-gray-700">
              Hình ảnh sản phẩm
            </label>

            {/* Thẻ input file ẩn được kích hoạt qua giao diện tải file trực quan */}
            <input
              ref={fileInputRef}
              type="file"
              accept={ALLOWED_IMAGE_TYPES.join(',')}
              className="hidden"
              onChange={handleFileInputChange}
              disabled={isSubmitting}
            />

            {!previewUrl ? (
              /* Vùng kéo thả / chọn file khi chưa có ảnh */
              <div
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
                className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed p-6 text-center cursor-pointer transition-all duration-200 ${
                  isDragging
                    ? 'border-primary-500 bg-primary-50/50 scale-[0.99]'
                    : 'border-gray-200 hover:border-primary-400 hover:bg-gray-50/60 bg-gray-50/30'
                }`}
              >
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white shadow-xs text-primary-600 mb-3">
                  <UploadCloud size={24} />
                </div>
                <p className="text-sm font-semibold text-gray-700">
                  <span className="text-primary-600 underline">Bấm để chọn file ảnh</span> hoặc kéo thả ảnh vào đây
                </p>
                <p className="mt-1 text-xs text-gray-400">
                  Hỗ trợ PNG, JPG, JPEG, WEBP, GIF (Dung lượng tối đa 10MB)
                </p>
              </div>
            ) : (
              /* Khung xem trước ảnh và thanh công cụ đổi / gỡ ảnh */
              <div className="space-y-2.5">
                <ImagePreview
                  url={previewUrl}
                  alt={currentProductName || 'Ảnh xem trước sản phẩm'}
                  heightClass="h-44 sm:h-48"
                />

                <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-gray-200 bg-gray-50/60 p-2.5 text-xs">
                  <div className="flex items-center gap-2 text-gray-700 font-medium truncate max-w-[280px]">
                    <FileImage size={16} className="shrink-0 text-primary-600" />
                    <span className="truncate">
                      {selectedFile ? selectedFile.name : 'Ảnh hiện tại của sản phẩm'}
                    </span>
                    {selectedFile && (
                      <span className="shrink-0 text-[11px] text-gray-400">
                        ({formatFileSize(selectedFile.size)})
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    <button
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                      disabled={isSubmitting}
                      className="inline-flex items-center gap-1 rounded-lg border border-gray-200 bg-white px-2.5 py-1 text-xs font-medium text-gray-700 hover:bg-gray-100 transition-colors disabled:opacity-50"
                    >
                      <RefreshCw size={13} />
                      <span>Đổi ảnh</span>
                    </button>
                    <button
                      type="button"
                      onClick={handleRemoveImage}
                      disabled={isSubmitting}
                      className="inline-flex items-center gap-1 rounded-lg border border-red-200 bg-red-50/50 px-2.5 py-1 text-xs font-medium text-red-600 hover:bg-red-100/70 transition-colors disabled:opacity-50"
                    >
                      <Trash2 size={13} />
                      <span>Gỡ ảnh</span>
                    </button>
                  </div>
                </div>
              </div>
            )}

            {fileError && (
              <div className="flex items-center gap-1.5 text-xs font-medium text-red-500">
                <AlertCircle size={14} className="shrink-0" />
                <span>{fileError}</span>
              </div>
            )}
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3 border-t border-gray-100 pt-4">
          <Button type="button" variant="secondary" onClick={handleClose} disabled={isSubmitting}>
            Hủy
          </Button>
          <Button type="submit" loading={isSubmitting} disabled={isSubmitting}>
            {isUploading ? 'Đang tải ảnh...' : isEdit ? 'Lưu thay đổi' : 'Thêm sản phẩm'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default AddProductModal
