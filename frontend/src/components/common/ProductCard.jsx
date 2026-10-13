import { useState } from 'react'
import { Package, Plus } from 'lucide-react'
import { formatCurrency } from '../../utils/formatters'
import Badge from './Badge'

/**
 * ProductCard component for POS catalog.
 * Displays product image, name, price (VND), and stock label ("SL: X").
 * Blurs and disables selection when stockQuantity == 0 with "Hết hàng" label.
 */
function ProductCard({ product, onSelect }) {
  const [imgError, setImgError] = useState(false)
  const stockQuantity = Number(product?.stockQuantity ?? product?.stock ?? 0)
  const isOutOfStock = stockQuantity === 0

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
          ? 'cursor-not-allowed border-gray-200 opacity-50 select-none grayscale-[25%]'
          : 'cursor-pointer border-gray-100 shadow-card hover:-translate-y-1 hover:border-primary-400 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-primary-400'
      }`}
    >
      {/* Top Image Section */}
      <div className="relative h-40 w-full overflow-hidden bg-gray-50 flex items-center justify-center">
        {product.image && !imgError ? (
          <img
            src={product.image}
            alt={product.name}
            onError={() => setImgError(true)}
            className={`h-full w-full object-cover transition-transform duration-300 ${
              isOutOfStock ? '' : 'group-hover:scale-105'
            }`}
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 text-gray-300">
            <Package size={44} strokeWidth={1.5} />
          </div>
        )}

        {/* Overlay Label for Out of Stock */}
        {isOutOfStock && (
          <div className="absolute inset-0 flex items-center justify-center bg-gray-900/60 backdrop-blur-[2px]">
            <span className="rounded-full bg-red-600 px-3 py-1 text-xs font-bold uppercase tracking-wider text-white shadow-md">
              Hết hàng
            </span>
          </div>
        )}
      </div>

      {/* Product Information */}
      <div className="flex flex-1 flex-col p-4">
        <h3
          className={`line-clamp-2 min-h-[2.5rem] text-sm font-semibold text-gray-800 ${
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

        {/* Action Button */}
        <button
          type="button"
          disabled={isOutOfStock}
          onClick={(e) => {
            e.stopPropagation()
            handleClick(e)
          }}
          className={`mt-4 flex w-full items-center justify-center gap-1.5 rounded-xl py-2 text-xs font-semibold transition-all ${
            isOutOfStock
              ? 'cursor-not-allowed bg-gray-100 text-gray-400'
              : 'bg-primary-50 text-primary-600 hover:bg-primary-500 hover:text-white shadow-sm'
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
