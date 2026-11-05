import { useState } from 'react'
import { Dumbbell, Plus } from 'lucide-react'
import { formatCurrency } from '../../utils/formatters'
import Badge from './Badge'

export const DEFAULT_GYM_PLACEHOLDER =
  'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?q=80&w=600'

/**
 * ProductCard component cho Màn hình Bán lẻ Mini POS (UC09).
 * - Đưa ảnh đại diện lên vị trí nổi bật, cố định tỷ lệ khung hình chuẩn 4:3 tránh xô lệch lưới.
 * - Cơ chế fallback onError 2 tầng:
 *   1. Đổi sang ảnh chất lượng cao mặc định của phòng gym (DEFAULT_GYM_PLACEHOLDER).
 *   2. Nếu offline / link dự phòng lỗi -> đổi sang icon thương hiệu phòng gym trực quan.
 * - Làm mờ và vô hiệu hóa chọn khi hết hàng (stock = 0).
 */
function ProductCard({ product, onSelect }) {
  const initialUrl = product?.imageUrl || product?.image || DEFAULT_GYM_PLACEHOLDER
  const [imgSrc, setImgSrc] = useState(initialUrl)
  const [hasError, setHasError] = useState(false)

  const stockQuantity = Number(product?.stockQuantity ?? product?.stock ?? 0)
  const isOutOfStock = stockQuantity === 0

  const handleImageError = () => {
    // Nếu link ảnh gốc bị chết hoặc tải lỗi, chuyển sang ảnh gym mặc định
    if (imgSrc !== DEFAULT_GYM_PLACEHOLDER) {
      setImgSrc(DEFAULT_GYM_PLACEHOLDER)
    } else {
      // Nếu ảnh mặc định cũng không load được (mất mạng/offline), bật cờ render icon mặc định
      setHasError(true)
    }
  }

  const handleClick = (e) => {
    if (isOutOfStock) {
      e.preventDefault()
      e.stopPropagation()
      return
    }
    if (onSelect) {
      onSelect(product)
    }
  }

  return (
    <div
      onClick={handleClick}
      aria-disabled={isOutOfStock}
      tabIndex={isOutOfStock ? -1 : 0}
      onKeyDown={(e) => {
        if (!isOutOfStock && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault()
          handleClick(e)
        }
      }}
      className={`group relative flex flex-col justify-between overflow-hidden rounded-2xl border bg-white transition-all duration-200 ${
        isOutOfStock
          ? 'cursor-not-allowed border-gray-200 opacity-60 select-none grayscale-[25%]'
          : 'cursor-pointer border-gray-100 shadow-card hover:-translate-y-1 hover:border-primary-400 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-primary-400'
      }`}
    >
      {/* Vị trí ảnh đại diện nổi bật với tỷ lệ khung hình cố định 4:3 (aspect-[4/3]) */}
      <div className="relative w-full aspect-[4/3] overflow-hidden bg-gray-50 flex items-center justify-center">
        {!hasError ? (
          <img
            src={imgSrc}
            alt={product.name}
            onError={handleImageError}
            loading="lazy"
            className={`h-full w-full object-cover transition-transform duration-300 ${
              isOutOfStock ? '' : 'group-hover:scale-105'
            }`}
          />
        ) : (
          /* Fallback icon mặc định của phòng gym khi link ảnh bị lỗi hoàn toàn */
          <div
            data-testid="gym-fallback-icon"
            className="flex h-full w-full flex-col items-center justify-center gap-1.5 bg-gradient-to-br from-primary-50/60 to-gray-100 text-primary-400 select-none"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white shadow-xs">
              <Dumbbell size={26} strokeWidth={2} className="text-primary-500" />
            </div>
            <span className="text-[10px] font-bold uppercase tracking-wider text-primary-600">
              PowerFit Gym
            </span>
          </div>
        )}

        {/* Overlay nhãn Hết hàng khi tồn kho = 0 */}
        {isOutOfStock && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-900/60 backdrop-blur-[2px]">
            <span className="rounded-full bg-red-600 px-3.5 py-1 text-xs font-bold uppercase tracking-wider text-white shadow-md">
              Hết hàng
            </span>
          </div>
        )}
      </div>

      {/* Thông tin sản phẩm */}
      <div className="flex flex-1 flex-col p-3.5 sm:p-4">
        {product.sku && (
          <span className="font-mono text-[10px] font-medium uppercase text-gray-500">
            {product.sku}
          </span>
        )}

        <h3
          className={`mt-1 line-clamp-2 min-h-[2.5rem] text-sm font-semibold text-gray-800 ${
            isOutOfStock ? '' : 'group-hover:text-primary-600'
          } transition-colors`}
          title={product.name}
        >
          {product.name}
        </h3>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-1.5">
          <span className="text-sm font-bold text-primary-600 whitespace-nowrap sm:text-base">
            {formatCurrency(product.price)}
          </span>

          <div className="shrink-0">
            {isOutOfStock ? (
              <Badge color="darkRed">Hết hàng</Badge>
            ) : (
              <Badge color={stockQuantity <= (product.minStock || 5) ? 'amber' : 'green'}>
                SL: {stockQuantity}
              </Badge>
            )}
          </div>
        </div>

        {/* Nút hành động thêm vào giỏ */}
        <button
          type="button"
          disabled={isOutOfStock}
          onClick={(e) => {
            e.stopPropagation()
            handleClick(e)
          }}
          className={`mt-3.5 flex w-full items-center justify-center gap-1.5 rounded-xl py-2 text-xs font-semibold transition-all ${
            isOutOfStock
              ? 'cursor-not-allowed bg-gray-100 text-gray-400'
              : 'bg-primary-50 text-primary-600 hover:bg-primary-500 hover:text-white shadow-xs'
          }`}
        >
          <Plus size={15} />
          {isOutOfStock ? 'Hết hàng' : 'Chọn mặt hàng'}
        </button>
      </div>
    </div>
  )
}

export default ProductCard
