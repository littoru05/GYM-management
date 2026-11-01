import axiosClient from '../api/axiosClient.js'
import { resolveDateRange } from './reportService.js'
import { mockRequest, generateId } from './api.js'

let transactionsStore = []

/**
 * Filter transactions by date range and type from Backend and store.
 */
export async function getTransactions({
  limit,
  range,
  startDate,
  endDate,
  type,
  simulateEmpty = false,
} = {}) {
  if (simulateEmpty) return []

  const { start, end } = resolveDateRange(range, startDate, endDate)

  try {
    const [subsRes, posRes] = await Promise.all([
      axiosClient.get('/subscriptions').catch(() => ({ data: [] })),
      axiosClient.get('/pos/orders/today').catch(() => ({ data: [] })),
    ])

    const subsList = Array.isArray(subsRes.data) ? subsRes.data : (subsRes.data?.data || [])
    const posList = Array.isArray(posRes.data) ? posRes.data : (posRes.data?.data || [])

    const mappedSubs = subsList.map((s) => ({
      id: s.code || `SUB-${s.id}`,
      date: (s.createdAt || s.startDate || '').slice(0, 10),
      type: 'MEMBERSHIP',
      memberName: s.memberName || s.memberCode || `Hội viên #${s.memberId}`,
      description: `Đăng ký gói tập ${s.membershipName || ''}`,
      amount: Number(s.paidPrice || 0),
      paymentMethod: s.paymentMethod || 'CASH',
      staffName: 'Quầy lễ tân',
    }))

    const mappedPos = posList.map((o) => ({
      id: o.orderCode || `ORD-${o.id}`,
      date: (o.orderTime || new Date().toISOString()).slice(0, 10),
      type: 'RETAIL',
      memberName: o.memberName || 'Khách lẻ',
      description: `Bán lẻ quầy POS (${(o.items || []).length} mặt hàng)`,
      amount: Number(o.totalAmount || 0),
      paymentMethod: o.paymentMethod || 'CASH',
      staffName: o.staffName || 'Thu ngân POS',
    }))

    let list = [...mappedSubs, ...mappedPos, ...transactionsStore]

    // Sort descending by date
    list.sort((a, b) => (b.date || '').localeCompare(a.date || ''))

    if (start && end) {
      list = list.filter((t) => t.date >= start && t.date <= end)
    }

    if (type) {
      list = list.filter((t) => t.type === type)
    }

    if (limit && limit > 0) {
      list = list.slice(0, limit)
    }

    return list
  } catch {
    let list = [...transactionsStore]
    if (start && end) {
      list = list.filter((t) => t.date >= start && t.date <= end)
    }
    if (type) {
      list = list.filter((t) => t.type === type)
    }
    if (limit && limit > 0) {
      list = list.slice(0, limit)
    }
    return list
  }
}

export function getTransactionsInRange(startDate, endDate) {
  return mockRequest(() =>
    transactionsStore.filter((t) => t.date >= startDate && t.date <= endDate)
  )
}

/**
 * Records a POS retail sale: creates a sale transaction record and returns invoice details.
 */
export function createRetailSale({
  cartItems,
  total,
  paymentMethod,
  staffName,
  discount = 0,
  subtotal,
}) {
  return mockRequest(async () => {
    const calculatedSubtotal =
      subtotal !== undefined
        ? subtotal
        : cartItems.reduce((s, i) => s + (Number(i.price) || 0) * i.quantity, 0)
    const finalTotal = total !== undefined ? total : Math.max(0, calculatedSubtotal - (discount || 0))

    const transaction = {
      id: generateId('TX'),
      date: new Date().toISOString().slice(0, 10),
      type: 'RETAIL',
      memberName: 'Khách lẻ',
      description: `Bán hàng tại quầy (${cartItems.length} mặt hàng)`,
      amount: finalTotal,
      paymentMethod,
      staffName: staffName || 'Nhân viên thu ngân',
    }

    transactionsStore = [transaction, ...transactionsStore]

    const invoice = {
      id: generateId('HD'),
      transactionId: transaction.id,
      date: transaction.date,
      memberName: 'Khách lẻ',
      items: cartItems.map((item) => ({
        name: item.name,
        quantity: item.quantity,
        price: item.price,
      })),
      subtotal: calculatedSubtotal,
      discount: discount || 0,
      discountAmount: discount || 0,
      total: finalTotal,
      paymentMethod,
      staffName: staffName || 'Nhân viên thu ngân',
    }

    return { transaction, invoice }
  }, 400)
}
