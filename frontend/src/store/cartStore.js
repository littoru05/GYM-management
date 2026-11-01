import { create } from 'zustand'
import { showSuccessToast, showErrorToast } from '../utils/toastService'

export const useCartStore = create((set, get) => ({
  items: [],
  discount: 0,

  /**
   * Cập nhật số tiền giảm giá
   */
  setDiscount: (val) => {
    const num = Math.max(0, Number(val) || 0)
    set({ discount: num })
  },

  /**
   * Thêm sản phẩm vào giỏ hàng:
   * - Nếu chưa có: thêm mới số lượng là 1.
   * - Nếu đã có: tăng số lượng lên 1 (kiểm tra không vượt quá tồn kho).
   */
  addItem: (product) => {
    const stockQuantity = Number(product.stockQuantity ?? product.stock ?? 0)
    if (stockQuantity <= 0) {
      showErrorToast('Sản phẩm đã hết hàng!')
      return false
    }

    const { items } = get()
    const existing = items.find((item) => item.id === product.id)

    if (existing) {
      if (existing.quantity >= stockQuantity) {
        showErrorToast('Không thể chọn vượt quá số lượng tồn kho')
        return false
      }
      set({
        items: items.map((item) =>
          item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
        ),
      })
      showSuccessToast(`Đã tăng số lượng "${product.name}" (${existing.quantity + 1}/${stockQuantity})`)
      return true
    } else {
      set({
        items: [
          ...items,
          {
            ...product,
            quantity: 1,
            stockQuantity,
            stock: stockQuantity,
          },
        ],
      })
      showSuccessToast(`Đã thêm "${product.name}" vào giỏ hàng`)
      return true
    }
  },

  /**
   * Tăng số lượng sản phẩm:
   * - Kiểm tra nếu số lượng trong giỏ đã bằng tồn kho thì vô hiệu hóa/bắn cảnh báo.
   */
  increaseQuantity: (id) => {
    const { items } = get()
    const item = items.find((i) => i.id === id)
    if (!item) return false

    const stock = Number(item.stockQuantity ?? item.stock ?? 0)
    if (item.quantity >= stock) {
      showErrorToast('Không thể chọn vượt quá số lượng tồn kho')
      return false
    }

    set({
      items: items.map((i) =>
        i.id === id ? { ...i, quantity: i.quantity + 1 } : i
      ),
    })
    return true
  },

  /**
   * Giảm số lượng sản phẩm:
   * - Nếu số lượng giảm về 0 (từ 1 xuống 0) thì tự động xóa món khỏi giỏ hàng.
   */
  decreaseQuantity: (id) => {
    const { items } = get()
    const item = items.find((i) => i.id === id)
    if (!item) return

    if (item.quantity <= 1) {
      // Giảm về 0 thì tự động xóa món khỏi giỏ hàng
      set({ items: items.filter((i) => i.id !== id) })
      showSuccessToast(`Đã xóa "${item.name}" khỏi giỏ hàng`)
    } else {
      set({
        items: items.map((i) =>
          i.id === id ? { ...i, quantity: i.quantity - 1 } : i
        ),
      })
    }
  },

  /**
   * Xóa một mặt hàng khỏi giỏ
   */
  removeItem: (id) => {
    const item = get().items.find((i) => i.id === id)
    set({ items: get().items.filter((i) => i.id !== id) })
    if (item) {
      showSuccessToast(`Đã xóa "${item.name}" khỏi giỏ hàng`)
    }
  },

  /**
   * Xóa toàn bộ giỏ hàng
   */
  clearCart: (silent = false) => {
    set({ items: [], discount: 0 })
    if (!silent) {
      showSuccessToast('Đã xóa toàn bộ giỏ hàng')
    }
  },

  /**
   * Tổng tiền tạm tính giỏ hàng
   */
  getSubtotal: () =>
    get().items.reduce((sum, item) => sum + (Number(item.price) || 0) * item.quantity, 0),

  /**
   * Tổng tiền thanh toán cuối cùng sau giảm giá
   */
  getTotal: () => {
    const subtotal = get().getSubtotal()
    const discount = Number(get().discount) || 0
    return Math.max(0, subtotal - discount)
  },

  /**
   * Tổng số lượng sản phẩm
   */
  getTotalQuantity: () =>
    get().items.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0),
}))
