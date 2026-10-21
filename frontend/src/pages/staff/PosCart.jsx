import { useState, useEffect } from 'react'
import {
  Minus,
  Plus,
  Trash2,
  ShoppingCart,
  Banknote,
  QrCode,
  CreditCard,
  Tag,
  X,
  Loader2,
  AlertCircle,
  User,
} from 'lucide-react'
import { useCartStore } from '../../store/cartStore'
import { formatCurrency } from '../../utils/formatters'
import { PAYMENT_METHOD, PAYMENT_METHOD_LABEL } from '../../utils/constants'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'

const PAYMENT_ICONS = {
  [PAYMENT_METHOD.CASH]: Banknote,
  [PAYMENT_METHOD.BANK_TRANSFER]: QrCode,
  [PAYMENT_METHOD.CARD]: CreditCard,
}

/**
 * PosCart component:
 * Bảng giỏ hàng bên phải màn hình POS:
 * - Hỗ trợ tăng giảm số lượng cực nhanh, kiểm soát giới hạn tồn kho.
 * - Hiển thị khu vực tính tiền: Tạm tính, Giảm giá (nếu có), Tổng tiền thanh toán to bản.
 * - Nhóm nút radio/button chọn hình thức thanh toán (Tiền mặt, Chuyển khoản QR, Thẻ ngân hàng).
 * - Nút lớn "Thanh toán (F9)" kèm hiệu ứng loading và phím tắt F9.
 * - Xác nhận popup khi xóa toàn bộ giỏ hàng.
 * - Bắt và hiển thị alert đỏ cảnh báo lỗi từ chối của Backend (400 Bad Request thiếu tồn kho).
 * - Hiển thị thông tin nhân viên thu ngân ca trực từ Auth store.
 */
function PosCart({
  paymentMethod = PAYMENT_METHOD.CASH,
  setPaymentMethod,
  onCheckout,
  isCheckingOut = false,
  checkoutError = null,
  onClearCheckoutError,
  cashierName = '',
  cashierCode = '',
}) {
  const [showClearConfirm, setShowClearConfirm] = useState(false)
  const [showDiscountInput, setShowDiscountInput] = useState(false)
  const [discountInputVal, setDiscountInputVal] = useState('')

  const {
    items,
    discount,
    setDiscount,
    increaseQuantity,
    decreaseQuantity,
    removeItem,
    clearCart,
    getSubtotal,
    getTotal,
    getTotalQuantity,
  } = useCartStore()

  const subtotal = getSubtotal()
  const total = getTotal()
  const totalQuantity = getTotalQuantity()

  // Phím tắt F9: Kích hoạt thanh toán nhanh
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'F9') {
        e.preventDefault()
        if (items.length === 0) {
          toast.error('Giỏ hàng đang trống, vui lòng chọn sản phẩm trước!')
          return
        }
        if (!isCheckingOut && onCheckout) {
          onCheckout()
        }
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [items.length, isCheckingOut, onCheckout])

  const handleIncrease = (item) => {
    onClearCheckoutError?.()
    const stock = Number(item.stockQuantity ?? item.stock ?? 0)
    if (item.quantity >= stock) {
      toast.error('Không thể chọn vượt quá số lượng tồn kho')
      return
    }
    increaseQuantity(item.id)
  }

  const handleDecrease = (id) => {
    onClearCheckoutError?.()
    decreaseQuantity(id)
  }

  const handleRemove = (id) => {
    onClearCheckoutError?.()
    removeItem(id)
  }

  const handleConfirmClear = () => {
    onClearCheckoutError?.()
    clearCart()
    setShowClearConfirm(false)
  }

  const handleApplyDiscount = (val) => {
    onClearCheckoutError?.()
    const numericVal = Number(val)
    if (numericVal < 0 || (typeof val === 'string' && val.trim().startsWith('-'))) {
      toast.error('Số tiền giảm giá không được âm!')
      setDiscountInputVal('')
      return
    }
    const num = Math.min(subtotal, Math.max(0, numericVal || 0))
    setDiscount(num)
    setDiscountInputVal('')
    setShowDiscountInput(false)
    if (num > 0) {
      toast.success(`Đã áp dụng giảm giá ${formatCurrency(num)}`)
    }
  }

  const handleApplyPercentDiscount = (percent) => {
    onClearCheckoutError?.()
    const num = Math.round((subtotal * percent) / 100)
    setDiscount(num)
    setShowDiscountInput(false)
    toast.success(`Đã áp dụng giảm giá ${percent}% (${formatCurrency(num)})`)
  }

  const handleRemoveDiscount = () => {
    onClearCheckoutError?.()
    setDiscount(0)
    toast.success('Đã gỡ bỏ giảm giá')
  }

  return (
    <div className="flex flex-col h-full max-h-[calc(100vh-4.5rem)]">
      {/* Header */}
      <div className="shrink-0 flex items-center justify-between pb-3 border-b border-gray-100">
        <div>
          <h2 className="text-base font-bold text-gray-900">Đơn hàng hiện tại</h2>
          <p className="text-xs text-gray-500">
            {items.length > 0
              ? `${items.length} mặt hàng • ${totalQuantity} món`
              : 'Chưa có sản phẩm nào'}
          </p>
        </div>

        {items.length > 0 && (
          <button
            type="button"
            onClick={() => setShowClearConfirm(true)}
            className="flex items-center gap-1.5 rounded-lg px-2 py-1 text-xs font-semibold text-red-500 hover:bg-red-50 hover:text-red-700 transition-colors"
            title="Xóa toàn bộ giỏ hàng"
          >
            <Trash2 size={13} />
            <span>Xóa toàn bộ</span>
          </button>
        )}
      </div>

      {/* Cashier Badge */}
      {cashierName && (
        <div className="shrink-0 mt-2.5 flex items-center justify-between rounded-lg bg-gray-50 px-2.5 py-1.5 text-xs text-gray-600 border border-gray-100">
          <div className="flex items-center gap-1.5">
            <User size={13} className="text-primary-600" />
            <span className="text-gray-500 font-medium">Thu ngân ca trực:</span>
          </div>
          <span className="font-semibold text-gray-800">
            {cashierName} {cashierCode ? `(${cashierCode})` : ''}
          </span>
        </div>
      )}

      {/* Backend 400 Bad Request / Insufficient Stock Alert */}
      {checkoutError && (
        <div
          role="alert"
          className="shrink-0 mt-2.5 rounded-xl border border-red-300 bg-red-50 p-3 text-xs text-red-800 shadow-xs"
        >
          <div className="flex items-start gap-2">
            <AlertCircle size={16} className="mt-0.5 shrink-0 text-red-600" />
            <div className="flex-1 min-w-0">
              <p className="font-bold text-red-900">Từ chối thanh toán: Không đủ hàng tồn kho</p>
              <p className="mt-1 font-medium leading-relaxed text-red-800">{checkoutError}</p>
              <p className="mt-1 text-[11px] font-semibold text-red-700">
                👉 Vui lòng điều chỉnh lại số lượng hoặc xóa mặt hàng bị thiếu khỏi giỏ hàng.
              </p>
            </div>
            {onClearCheckoutError && (
              <button
                type="button"
                onClick={onClearCheckoutError}
                className="shrink-0 rounded p-1 text-red-500 hover:bg-red-100 hover:text-red-700 transition-colors"
                title="Đóng cảnh báo"
                aria-label="Đóng cảnh báo"
              >
                <X size={14} />
              </button>
            )}
          </div>
        </div>
      )}

      {/* Cart Items List / Empty State */}
      <div className="mt-3 flex-1 min-h-[140px] max-h-[calc(100vh-430px)] overflow-y-auto pr-1">
        {items.length === 0 ? (
          <EmptyState
            icon={ShoppingCart}
            title="Giỏ hàng trống"
            description="Chọn sản phẩm từ danh mục bên trái để thêm vào đơn hàng"
          />
        ) : (
          <div className="space-y-2.5">
            {items.map((item) => {
              const stock = Number(item.stockQuantity ?? item.stock ?? 0)
              const isMaxStock = item.quantity >= stock
              const isProblemItem = Boolean(
                checkoutError &&
                  ((item.name && checkoutError.toLowerCase().includes(item.name.toLowerCase())) ||
                    (item.sku && checkoutError.toLowerCase().includes(item.sku.toLowerCase())))
              )

              return (
                <div
                  key={item.id}
                  className={`flex items-start gap-3 rounded-xl border p-3 shadow-xs transition-colors ${
                    isProblemItem
                      ? 'border-red-400 bg-red-50/40 ring-1 ring-red-300'
                      : 'border-gray-100 bg-white hover:border-gray-200'
                  }`}
                >
                  {/* Thumbnail */}
                  <div className="h-12 w-12 shrink-0 overflow-hidden rounded-lg border border-gray-100 bg-gray-50 flex items-center justify-center">
                    {item.image ? (
                      <img src={item.image} alt={item.name} className="h-full w-full object-cover" />
                    ) : (
                      <span className="text-[10px] text-gray-400 font-medium">Ảnh</span>
                    )}
                  </div>

                  {/* Details */}
                  <div className="flex-1 min-w-0">
                    {/* Dòng 1: Tên sản phẩm chiếm 1 dòng và nút xóa icon thùng rác */}
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-semibold text-gray-800" title={item.name}>
                          {item.name}
                        </p>
                        <p className="text-xs text-gray-400 font-medium">
                          Đơn giá: {formatCurrency(item.price)}
                        </p>
                        {isProblemItem && (
                          <span className="mt-1 inline-flex items-center gap-1 rounded bg-red-100 px-1.5 py-0.5 text-[10px] font-bold text-red-700">
                            <AlertCircle size={11} /> Cần điều chỉnh số lượng
                          </span>
                        )}
                      </div>

                      <button
                        type="button"
                        onClick={() => handleRemove(item.id)}
                        className="shrink-0 rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600 transition-colors"
                        title="Xóa món này"
                        aria-label="Xóa món này"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>

                    {/* Dòng 2: Cụm nút điều chỉnh số lượng [-] [SL] [+] và Thành tiền */}
                    <div className="mt-2.5 flex items-center justify-between gap-2 border-t border-gray-50 pt-2">
                      {/* Cụm nút điều chỉnh số lượng */}
                      <div className="flex items-center gap-1.5">
                        <button
                          type="button"
                          onClick={() => handleDecrease(item.id)}
                          className="flex h-7 w-7 items-center justify-center rounded-lg border border-gray-200 bg-white text-gray-600 hover:bg-gray-100 transition-colors active:scale-95"
                          title={item.quantity === 1 ? 'Giảm về 0 sẽ xóa món' : 'Giảm 1'}
                          aria-label="Giảm số lượng"
                        >
                          <Minus size={13} />
                        </button>

                        <span className="w-7 text-center text-sm font-bold text-gray-800 select-none">
                          {item.quantity}
                        </span>

                        <button
                          type="button"
                          aria-disabled={isMaxStock}
                          onClick={() => handleIncrease(item)}
                          className={`flex h-7 w-7 items-center justify-center rounded-lg border transition-colors ${
                            isMaxStock
                              ? 'cursor-not-allowed border-gray-200 bg-gray-100 text-gray-400 opacity-60'
                              : 'border-gray-200 bg-white text-gray-600 hover:bg-gray-100 active:scale-95'
                          }`}
                          title={isMaxStock ? 'Không thể chọn vượt quá số lượng tồn kho' : 'Tăng 1'}
                          aria-label="Tăng số lượng"
                        >
                          <Plus size={13} />
                        </button>

                        {isMaxStock && (
                          <span className="text-[10px] font-semibold text-amber-600">
                            (Tối đa)
                          </span>
                        )}
                      </div>

                      {/* Thành tiền từng món */}
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
        )}
      </div>

      {/* Footer: Tính tiền, Phương thức thanh toán, Nút thanh toán (F9) */}
      <div className="shrink-0 mt-3 space-y-2.5 border-t border-gray-100 pt-2.5">
        {/* Nhóm nút radio chọn hình thức thanh toán */}
        {setPaymentMethod && (
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-gray-700">Hình thức thanh toán:</label>
              <span className="text-[11px] font-medium text-gray-400">
                {PAYMENT_METHOD_LABEL[paymentMethod]}
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2">
              {Object.values(PAYMENT_METHOD).map((method) => {
                const Icon = PAYMENT_ICONS[method]
                const isSelected = paymentMethod === method
                return (
                  <button
                    key={method}
                    type="button"
                    onClick={() => setPaymentMethod(method)}
                    className={`relative flex flex-col items-center gap-1.5 rounded-xl border p-2 text-xs font-medium transition-all ${
                      isSelected
                        ? 'border-primary-500 bg-primary-50/80 text-primary-700 font-bold shadow-sm ring-1 ring-primary-400'
                        : 'border-gray-200 bg-white text-gray-600 hover:bg-gray-50 hover:border-gray-300'
                    }`}
                  >
                    {/* Radio indicator */}
                    <div className="absolute top-1.5 right-1.5 flex h-3.5 w-3.5 items-center justify-center rounded-full border border-gray-300 bg-white">
                      {isSelected && (
                        <div className="h-2 w-2 rounded-full bg-primary-600" />
                      )}
                    </div>

                    <Icon size={18} className={isSelected ? 'text-primary-600' : 'text-gray-500'} />
                    <span className="text-center leading-tight line-clamp-1">{PAYMENT_METHOD_LABEL[method]}</span>
                  </button>
                )
              })}
            </div>
          </div>
        )}

        {/* Khu vực tính tiền: Tạm tính, Giảm giá (nếu có), Tổng tiền thanh toán */}
        <div className="space-y-1.5 rounded-xl bg-gray-50/80 p-3 border border-gray-100">
          {/* Tạm tính */}
          <div className="flex items-center justify-between text-xs text-gray-600">
            <span>Tạm tính:</span>
            <span className="font-semibold text-gray-800">{formatCurrency(subtotal)}</span>
          </div>

          {/* Giảm giá (nếu có) */}
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5">
              <span className="text-gray-600">Giảm giá:</span>
              {!showDiscountInput && discount === 0 && items.length > 0 && (
                <button
                  type="button"
                  onClick={() => setShowDiscountInput(true)}
                  className="inline-flex items-center gap-0.5 rounded px-1.5 py-0.5 text-[10px] font-semibold text-primary-600 hover:bg-primary-50 transition-colors"
                >
                  <Tag size={11} />
                  <span>+ Thêm giảm giá</span>
                </button>
              )}
            </div>

            {discount > 0 ? (
              <div className="flex items-center gap-1 font-bold text-emerald-600">
                <span>-{formatCurrency(discount)}</span>
                <button
                  type="button"
                  onClick={handleRemoveDiscount}
                  className="rounded p-0.5 text-gray-400 hover:bg-red-50 hover:text-red-500"
                  title="Gỡ giảm giá"
                >
                  <X size={12} />
                </button>
              </div>
            ) : (
              <span className="font-medium text-gray-400">0 ₫</span>
            )}
          </div>

          {/* Ô nhập giảm giá mở rộng */}
          {showDiscountInput && (
            <div className="mt-2 space-y-2 rounded-lg border border-primary-200 bg-white p-2 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-semibold text-gray-700">Mức giảm giá:</span>
                <button
                  type="button"
                  onClick={() => setShowDiscountInput(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={12} />
                </button>
              </div>

              {/* Phím giảm giá nhanh */}
              <div className="flex gap-1.5">
                {[
                  { label: '5%', action: () => handleApplyPercentDiscount(5) },
                  { label: '10%', action: () => handleApplyPercentDiscount(10) },
                  { label: '20k', action: () => handleApplyDiscount(20000) },
                  { label: '50k', action: () => handleApplyDiscount(50000) },
                ].map((btn, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={btn.action}
                    className="flex-1 rounded border border-gray-200 bg-gray-50 py-1 text-[10px] font-medium text-gray-700 hover:bg-primary-50 hover:border-primary-300 hover:text-primary-600 transition-colors"
                  >
                    {btn.label}
                  </button>
                ))}
              </div>

              {/* Ô nhập số tiền tùy chọn */}
              <div className="flex gap-1.5">
                <input
                  type="number"
                  min="0"
                  max={subtotal}
                  step="1000"
                  placeholder="Số tiền giảm (VNĐ)..."
                  value={discountInputVal}
                  onChange={(e) => setDiscountInputVal(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault()
                      handleApplyDiscount(discountInputVal)
                    }
                  }}
                  className="flex-1 rounded-lg border border-gray-200 px-2 py-1 text-xs focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                />
                <button
                  type="button"
                  onClick={() => handleApplyDiscount(discountInputVal)}
                  className="rounded-lg bg-primary-500 px-2.5 py-1 text-xs font-bold text-white hover:bg-primary-600"
                >
                  Áp dụng
                </button>
              </div>
            </div>
          )}

          {/* Tổng tiền thanh toán bằng chữ số to bản, rõ ràng */}
          <div className="border-t border-gray-200/90 pt-2 flex items-baseline justify-between">
            <span className="text-sm font-extrabold text-gray-900 uppercase">Tổng thanh toán:</span>
            <span className="text-2xl font-black text-primary-600 tracking-tight">
              {formatCurrency(total)}
            </span>
          </div>
        </div>

        {/* Nút lớn "Thanh toán (F9)" kèm loading khi đang gửi request */}
        {onCheckout && (
          <button
            type="button"
            disabled={items.length === 0 || isCheckingOut}
            onClick={onCheckout}
            className={`flex w-full items-center justify-center gap-2 rounded-xl py-3.5 px-4 text-base font-extrabold shadow-md transition-all ${
              items.length === 0 || isCheckingOut
                ? 'cursor-not-allowed bg-gray-200 text-gray-400 opacity-70'
                : 'bg-primary-500 text-white hover:bg-primary-600 hover:shadow-lg active:scale-[0.99]'
            }`}
          >
            {isCheckingOut ? (
              <>
                <Loader2 size={19} className="animate-spin text-white" />
                <span>Đang xử lý đơn hàng...</span>
              </>
            ) : (
              <>
                <CreditCard size={18} />
                <span>Thanh toán (F9)</span>
                <span className="text-white/80 font-normal">•</span>
                <span>{formatCurrency(total)}</span>
              </>
            )}
          </button>
        )}
      </div>

      {/* Popup xác nhận nhanh khi xóa toàn bộ giỏ hàng */}
      <ConfirmDialog
        open={showClearConfirm}
        onClose={() => setShowClearConfirm(false)}
        onConfirm={handleConfirmClear}
        title="Xác nhận xóa giỏ hàng"
        message={`Bạn có chắc chắn muốn xóa toàn bộ ${items.length} mặt hàng (${totalQuantity} sản phẩm) khỏi giỏ hàng không? Thao tác này không thể hoàn tác.`}
        confirmLabel="Xác nhận xóa"
        cancelLabel="Giữ lại"
      />
    </div>
  )
}

export default PosCart
