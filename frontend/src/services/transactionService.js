import { mockRequest, generateId } from './api'

/**
 * Records a POS retail sale: creates a sale transaction record and returns invoice details.
 */
export function createRetailSale({ cartItems, total, paymentMethod, staffName, discount = 0, subtotal }) {
  return mockRequest(async () => {
    const calculatedSubtotal = subtotal !== undefined ? subtotal : cartItems.reduce((s, i) => s + (Number(i.price) || 0) * i.quantity, 0)
    const finalTotal = total !== undefined ? total : Math.max(0, calculatedSubtotal - (discount || 0))

    const transaction = {
      id: generateId('TX'),
      date: new Date().toISOString(),
      type: 'RETAIL',
      memberName: 'Khách lẻ',
      description: `Bán hàng tại quầy (${cartItems.length} mặt hàng)`,
      amount: finalTotal,
      paymentMethod,
      staffName: staffName || 'Nhân viên thu ngân',
    }

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
