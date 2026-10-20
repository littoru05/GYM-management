import { mockRequest, generateId } from './api.js'

let transactionsStore = []

/**
 * Filter transactions by date range and type.
 */
export function getTransactions({
  limit,
  range,
  startDate,
  endDate,
  type,
  simulateEmpty = false,
} = {}) {
  return mockRequest(() => {
    if (simulateEmpty) return []

    let list = [...transactionsStore]

    const today = new Date().toISOString().slice(0, 10)
    let start = startDate
    let end = endDate || today

    if (range === '7d') {
      const d = new Date()
      d.setDate(d.getDate() - 6)
      start = d.toISOString().slice(0, 10)
    } else if (range === '30d') {
      const d = new Date()
      d.setDate(d.getDate() - 29)
      start = d.toISOString().slice(0, 10)
    } else if (range === '12m') {
      const d = new Date()
      d.setMonth(d.getMonth() - 11)
      d.setDate(1)
      start = d.toISOString().slice(0, 10)
    }

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
  }, 200)
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
