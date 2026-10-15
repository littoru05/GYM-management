import axiosClient from '../api/axiosClient'

/**
 * Chuẩn hóa và trích xuất thông điệp lỗi từ response Backend (đặc biệt lỗi 400 Bad Request, thiếu hàng tồn kho).
 *
 * @param {any} error
 * @returns {string} Thông điệp lỗi chi tiết, dễ hiểu cho người dùng
 */
export function getCheckoutErrorMessage(error) {
  if (error?.response?.data) {
    const data = error.response.data

    // Lỗi có thông điệp chính từ backend (ví dụ: InsufficientStockException hoặc BadRequestException)
    if (data.message && typeof data.message === 'string') {
      if (data.errors && typeof data.errors === 'object' && Object.keys(data.errors).length > 0) {
        const details = Object.entries(data.errors)
          .map(([key, msg]) => `${msg || key}`)
          .join('; ')
        return `${data.message} (${details})`
      }
      return data.message
    }

    // Lỗi validation có errors map
    if (data.errors && typeof data.errors === 'object') {
      const msgs = Object.values(data.errors).filter(Boolean)
      if (msgs.length > 0) {
        return msgs.join('; ')
      }
    }

    if (typeof data === 'string' && data.trim()) {
      return data
    }
  }

  if (error?.code === 'ERR_NETWORK') {
    return 'Không thể kết nối đến máy chủ Backend (Cổng 8080). Vui lòng kiểm tra dịch vụ máy chủ.'
  }

  return error?.message || 'Thanh toán đơn hàng thất bại. Vui lòng kiểm tra lại giỏ hàng và thử lại.'
}

/**
 * Chuyển đổi dữ liệu đơn hàng trả về từ Backend (OrderResponseDTO) sang cấu trúc hóa đơn hiển thị / in phiếu thu.
 *
 * @param {Object} order - OrderResponseDTO từ Backend
 * @param {Object} [currentUser] - Thông tin nhân viên thu ngân từ Auth store
 * @returns {Object} Cấu trúc hóa đơn cho InvoiceReceiptModal
 */
export function mapOrderToInvoice(order, currentUser) {
  if (!order) return null

  const staffName =
    order.staffName ||
    currentUser?.name ||
    currentUser?.fullName ||
    'Thu ngân quầy'

  const items = Array.isArray(order.items)
    ? order.items.map((item) => ({
        id: item.id || item.productId,
        productId: item.productId,
        sku: item.productSku || '',
        name: item.productName || 'Sản phẩm',
        quantity: Number(item.quantity || 1),
        price: Number(item.unitPrice || 0),
        unitPrice: Number(item.unitPrice || 0),
        totalPrice: Number(
          item.totalPrice != null
            ? item.totalPrice
            : (item.unitPrice || 0) * (item.quantity || 1)
        ),
      }))
    : []

  return {
    id: order.code || (order.id ? `HD-${order.id}` : `HD-${Date.now()}`),
    code: order.code,
    transactionId: order.id || order.code,
    date: order.transactionTime || order.orderDate || new Date().toISOString(),
    staffId: order.staffId || currentUser?.id,
    staffName,
    memberId: order.memberId,
    memberName: order.memberName || 'Khách lẻ',
    paymentMethod: order.paymentMethod || 'CASH',
    subtotal: Number(order.subtotal || 0),
    discountAmount: Number(order.discountAmount || 0),
    discount: Number(order.discountAmount || 0),
    total: Number(order.totalAmount != null ? order.totalAmount : order.amount || 0),
    notes: order.notes || '',
    status: order.status || 'COMPLETED',
    items,
  }
}

/**
 * Gửi yêu cầu checkout đơn hàng tới Backend
 * Endpoint: POST /api/pos/checkout
 *
 * Payload backend yêu cầu (CheckoutRequestDTO):
 * - items: Array<{ productId: number, quantity: number }> (bắt buộc, không rỗng, quantity >= 1)
 * - paymentMethod: 'CASH' | 'BANK_TRANSFER' | 'CARD' (bắt buộc)
 * - memberId: number | null (tùy chọn)
 * - discountAmount: number (tùy chọn, mặc định 0)
 * - notes: string (tùy chọn)
 *
 * @param {Object} orderData
 * @param {Array<{ id?: number, productId?: number, quantity: number }>} orderData.items - Danh sách mặt hàng trong giỏ
 * @param {'CASH'|'BANK_TRANSFER'|'CARD'} orderData.paymentMethod - Phương thức thanh toán
 * @param {number|null} [orderData.memberId] - ID hội viên nếu có
 * @param {number} [orderData.discountAmount] - Số tiền giảm giá
 * @param {number} [orderData.discount] - Số tiền giảm giá (alias)
 * @param {string} [orderData.notes] - Ghi chú đơn hàng
 * @returns {Promise<Object>} OrderResponseDTO từ Backend
 */
export async function checkoutOrder(orderData) {
  if (!orderData || !Array.isArray(orderData.items) || orderData.items.length === 0) {
    throw new Error('Giỏ hàng trống. Vui lòng chọn ít nhất một sản phẩm trước khi thanh toán.')
  }

  // Chuẩn hóa payload đúng theo CheckoutRequestDTO Backend yêu cầu
  const payload = {
    items: orderData.items.map((item) => {
      const pId = Number(item.productId ?? item.id)
      const qty = Number(item.quantity)
      if (!pId || isNaN(pId)) {
        throw new Error(`Sản phẩm "${item.name || 'chưa rõ'}" thiếu mã ID hợp lệ.`)
      }
      if (!qty || qty <= 0 || isNaN(qty)) {
        throw new Error(`Số lượng mua của sản phẩm "${item.name || pId}" phải lớn hơn 0.`)
      }
      return {
        productId: pId,
        quantity: qty,
      }
    }),
    paymentMethod: orderData.paymentMethod || 'CASH',
    memberId: orderData.memberId ? Number(orderData.memberId) : null,
    discountAmount: Number(orderData.discountAmount ?? orderData.discount ?? 0),
    notes: orderData.notes || orderData.description || 'Bán lẻ tại quầy POS',
  }

  const response = await axiosClient.post('/pos/checkout', payload)
  return response.data
}

/**
 * Lấy chi tiết đơn hàng theo ID
 * Endpoint: GET /api/pos/orders/{id}
 *
 * @param {number|string} id
 * @returns {Promise<Object>} OrderResponseDTO
 */
export async function getOrderById(id) {
  const response = await axiosClient.get(`/pos/orders/${id}`)
  return response.data
}

/**
 * Lấy danh sách các đơn hàng trong ngày
 * Endpoint: GET /api/pos/orders/today
 *
 * @returns {Promise<Array<Object>>} Danh sách OrderResponseDTO
 */
export async function getTodayOrders() {
  const response = await axiosClient.get('/pos/orders/today')
  return response.data
}
