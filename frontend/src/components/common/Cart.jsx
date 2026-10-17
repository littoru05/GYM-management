import { Minus, Plus, Trash2, ShoppingCart } from 'lucide-react'
import EmptyState from './EmptyState'
import { formatCurrency } from '../../utils/formatters'

/**
 * POS shopping cart item list with 2-row layout:
 * - Row 1: Product name (full width) + delete button
 * - Row 2: Quantity selector (+-) and item total price
 */
function Cart({ items, onIncrease, onDecrease, onRemove }) {
  if (!items || items.length === 0) {
    return (
      <EmptyState
        icon={ShoppingCart}
        title="Giỏ hàng trống"
        description="Chọn sản phẩm từ danh mục để thêm vào đơn hàng"
      />
    )
  }

  return (
    <div className="space-y-2.5">
      {items.map((item) => {
        const itemStock = Number(item.stockQuantity ?? item.stock ?? 9999)
        const canIncrease = item.quantity < itemStock

        return (
          <div
            key={item.id}
            className="flex items-start gap-3 rounded-xl border border-gray-100 bg-white p-3 shadow-xs transition-colors hover:border-gray-200"
          >
            {/* Thumbnail */}
            <div className="h-12 w-12 shrink-0 overflow-hidden rounded-lg border border-gray-100 bg-gray-50 flex items-center justify-center">
              {item.image ? (
                <img src={item.image} alt={item.name} className="h-full w-full object-cover" />
              ) : (
                <span className="text-[10px] text-gray-400">Ảnh</span>
              )}
            </div>

            {/* Details */}
            <div className="flex-1 min-w-0">
              {/* Row 1: Product Name occupies full line & Delete Button */}
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold text-gray-800" title={item.name}>
                    {item.name}
                  </p>
                  <p className="text-xs text-gray-400 font-medium">
                    {formatCurrency(item.price)}
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => onRemove(item.id)}
                  className="shrink-0 p-1 text-gray-300 hover:text-red-500 transition-colors"
                  aria-label="Xóa khỏi đơn hàng"
                >
                  <Trash2 size={16} />
                </button>
              </div>

              {/* Row 2: Quantity Controls (+-) & Item Total Price */}
              <div className="mt-2.5 flex items-center justify-between gap-2 border-t border-gray-50 pt-2">
                {/* Quantity Controls */}
                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => onDecrease(item.id)}
                    className="flex h-7 w-7 items-center justify-center rounded-lg border border-gray-200 bg-white text-gray-600 hover:bg-gray-100 transition-colors active:scale-95"
                    aria-label="Giảm số lượng"
                  >
                    <Minus size={13} />
                  </button>
                  <span className="w-6 text-center text-sm font-bold text-gray-800">
                    {item.quantity}
                  </span>
                  <button
                    type="button"
                    onClick={() => onIncrease(item.id)}
                    disabled={!canIncrease}
                    className="flex h-7 w-7 items-center justify-center rounded-lg border border-gray-200 bg-white text-gray-600 hover:bg-gray-100 transition-colors active:scale-95 disabled:cursor-not-allowed disabled:opacity-40"
                    aria-label="Tăng số lượng"
                  >
                    <Plus size={13} />
                  </button>
                </div>

                {/* Subtotal */}
                <div className="text-right">
                  <span className="text-sm font-bold text-primary-600">
                    {formatCurrency(item.price * item.quantity)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default Cart
