export function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(value || 0)
}

export function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleDateString('vi-VN')
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('vi-VN')
}

export function formatNumber(value) {
  return new Intl.NumberFormat('vi-VN').format(value || 0)
}

export function daysUntil(dateStr) {
  if (!dateStr) return null
  const target = new Date(dateStr)
  const now = new Date()
  target.setHours(0, 0, 0, 0)
  now.setHours(0, 0, 0, 0)
  return Math.round((target - now) / (1000 * 60 * 60 * 24))
}

export function getMemberStatus(expiryDate) {
  const remaining = daysUntil(expiryDate)
  if (remaining === null) return 'EXPIRED'
  if (remaining < 0) return 'EXPIRED'
  if (remaining <= 7) return 'EXPIRING_SOON'
  return 'ACTIVE'
}

/**
 * Định dạng thời gian tương đối tự nhiên bằng tiếng Việt (ví dụ: "vừa xong", "5 phút trước", "2 giờ trước")
 * @param {string|number|Date} value - Giá trị thời gian cần định dạng
 * @returns {string} Chuỗi thời gian relative
 */
export function formatRelativeTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  const diffMs = Date.now() - date.getTime()
  if (diffMs < 0) return 'Vừa xong'

  const diffSec = Math.floor(diffMs / 1000)
  if (diffSec < 60) return 'Vừa xong'

  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin} phút trước`

  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour} giờ trước`

  const diffDays = Math.floor(diffHour / 24)
  if (diffDays < 30) return `${diffDays} ngày trước`

  const diffMonths = Math.floor(diffDays / 30)
  if (diffMonths < 12) return `${diffMonths} tháng trước`

  return `${Math.floor(diffMonths / 12)} năm trước`
}


