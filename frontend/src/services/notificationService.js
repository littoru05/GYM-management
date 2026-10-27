import axiosClient from '../api/axiosClient.js'

/**
 * Service lấy thông tin thông báo (từ liên hệ / leads mới)
 */
export async function getUnreadCount() {
  try {
    const response = await axiosClient.get('/notifications/unread-count')
    return response.data
  } catch (error) {
    // Nếu API backend chưa sẵn sàng hoặc gặp lỗi mạng, trả về mặc định để không gián đoạn UI
    if (import.meta.env?.DEV) {
      console.warn('Không thể kết nối API /notifications/unread-count, sử dụng fallback mặc định:', error?.message)
    }
    return { unreadCount: 0 }
  }
}

/**
 * Lấy danh sách thông báo / liên hệ mới nhất
 * @param {number} limit 
 */
export async function getRecentNotifications(limit = 5) {
  try {
    const response = await axiosClient.get('/notifications/recent', {
      params: { limit },
    })
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    if (import.meta.env?.DEV) {
      console.warn('Không thể kết nối API /notifications/recent, trả về mảng rỗng:', error?.message)
    }
    return []
  }
}

export default {
  getUnreadCount,
  getRecentNotifications,
}
