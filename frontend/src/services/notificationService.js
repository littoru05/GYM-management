import axiosClient from '../api/axiosClient.js'

/**
 * Service gọi API GET /api/notifications/unread-count
 * Lấy số lượng thông báo / liên hệ chưa đọc từ backend.
 * @returns {Promise<{ unreadCount: number }>}
 */
export async function getUnreadCount() {
  const response = await axiosClient.get('/notifications/unread-count')
  return response.data
}

/**
 * Service gọi API GET /api/notifications/recent
 * Lấy danh sách liên hệ / thông báo mới nhất.
 * @param {number} [limit=5] - Giới hạn số lượng liên hệ cần lấy
 * @returns {Promise<Array<{
 *   id: number,
 *   name: string,
 *   phone: string,
 *   email: string,
 *   message: string,
 *   createdAt: string,
 *   isRead: boolean,
 *   status: string
 * }>>}
 */
export async function getRecentNotifications(limit = 5) {
  const response = await axiosClient.get('/notifications/recent', {
    params: { limit },
  })
  return response.data
}

/**
 * Service gọi API PATCH /api/notifications/{id}/read
 * Đánh dấu một thông báo liên hệ là đã đọc.
 * @param {number|string} id - ID của thông báo liên hệ
 * @returns {Promise<Object>}
 */
export async function markAsRead(id) {
  const response = await axiosClient.patch(`/notifications/${id}/read`)
  return response.data
}

/**
 * Service gọi API PATCH /api/notifications/read-all
 * Đánh dấu tất cả thông báo liên hệ là đã đọc.
 * @returns {Promise<{ message: string, updatedCount: number }>}
 */
export async function markAllAsRead() {
  const response = await axiosClient.patch('/notifications/read-all')
  return response.data
}

/**
 * Thiết lập polling định kỳ gọi GET /api/notifications/unread-count
 * Gọi ngay lập tức khi nạp và định kỳ theo chu kỳ interval (45 giây - 60 giây).
 *
 * @param {Function} onUpdate - Callback nhận giá trị unreadCount mới
 * @param {number} [intervalMs=45000] - Chu kỳ lặp lại (mặc định 45s = 45000ms)
 * @returns {Function} Hàm cleanup (clearInterval) khi component unmount
 */
export function pollUnreadCount(onUpdate, intervalMs = 45000) {
  // Fetch on mount
  getUnreadCount()
    .then((data) => {
      if (typeof data?.unreadCount === 'number') {
        onUpdate(data.unreadCount)
      }
    })
    .catch((err) => {
      console.error('Lỗi khi fetch unread-count on mount:', err)
    })

  // Định kỳ gọi API ngầm
  const intervalId = setInterval(async () => {
    try {
      const data = await getUnreadCount()
      if (typeof data?.unreadCount === 'number') {
        onUpdate(data.unreadCount)
      }
    } catch (err) {
      console.error('Lỗi khi polling unread-count:', err)
    }
  }, intervalMs)

  // Trả về hàm cleanup clearInterval để tránh rò rỉ bộ nhớ khi unmount
  return () => {
    clearInterval(intervalId)
  }
}

export default {
  getUnreadCount,
  getRecentNotifications,
  markAsRead,
  markAllAsRead,
  pollUnreadCount,
}
