export const STOCK_STATUS = {
  OUT_OF_STOCK: { key: 'OUT_OF_STOCK', color: 'darkRed', label: 'Hết hàng' },
  LOW_STOCK: { key: 'LOW_STOCK', color: 'amber', label: 'Cảnh báo: Sắp hết' },
  IN_STOCK: { key: 'IN_STOCK', color: 'green', label: 'Đủ hàng' },
}

// Thứ tự kiểm tra quan trọng: hết hàng (=0) phải được xét trước mức cảnh báo (<= minStock)
export function getStockStatus(stockQuantity, minStock) {
  const quantity = Number(stockQuantity) || 0
  const threshold = Number(minStock) || 0

  if (quantity === 0) return STOCK_STATUS.OUT_OF_STOCK
  if (quantity <= threshold) return STOCK_STATUS.LOW_STOCK
  return STOCK_STATUS.IN_STOCK
}
