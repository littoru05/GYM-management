import axiosClient from '../api/axiosClient.js'
import { initialMockNotifications } from '../data/mockNotifications.js'

const STORAGE_KEY = 'gym_mock_notifications'

let inMemoryNotifications = null

/**
 * Lấy danh sách thông báo từ localStorage hoặc fallback về mock data ban đầu
 */
function getLocalNotifications() {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const stored = window.localStorage.getItem(STORAGE_KEY)
      if (stored) {
        const parsed = JSON.parse(stored)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed
        }
      }
      // Khởi tạo lần đầu
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(initialMockNotifications))
      return initialMockNotifications
    }
  } catch (err) {
    console.warn('Lỗi khi truy cập localStorage cho thông báo:', err)
  }

  if (!inMemoryNotifications) {
    inMemoryNotifications = initialMockNotifications.map((n) => ({ ...n }))
  }
  return inMemoryNotifications
}

/**
 * Lưu danh sách thông báo vào localStorage và bộ nhớ in-memory
 */
function saveLocalNotifications(notifications) {
  inMemoryNotifications = notifications
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(notifications))
    }
  } catch (err) {
    console.warn('Lỗi khi lưu thông báo vào localStorage:', err)
  }
}

/**
 * Service lấy số lượng thông báo chưa đọc
 */
export async function getUnreadCount() {
  try {
    const response = await axiosClient.get('/notifications/unread-count')
    if (response?.data && typeof response.data.unreadCount === 'number') {
      return response.data
    }
  } catch {
    // API backend chưa có sẵn, sử dụng mockData
  }

  const list = getLocalNotifications()
  const unreadCount = list.filter((item) => !item.isRead).length
  return { unreadCount }
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
    if (Array.isArray(response.data) && response.data.length > 0) {
      return response.data
    }
  } catch {
    // API backend chưa có sẵn, sử dụng mockData
  }

  const list = getLocalNotifications()
  const sorted = [...list].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  return sorted.slice(0, limit)
}

/**
 * Đánh dấu một thông báo là đã đọc
 * @param {number|string} id 
 */
export async function markAsRead(id) {
  try {
    await axiosClient.patch(`/notifications/${id}/read`)
  } catch {
    // Backend chưa hỗ trợ endpoint PATCH, xử lý tiếp ở local mock
  }

  const list = getLocalNotifications()
  const updated = list.map((item) => {
    if (String(item.id) === String(id)) {
      return { ...item, isRead: true }
    }
    return item
  })
  saveLocalNotifications(updated)
  return updated.find((item) => String(item.id) === String(id))
}

/**
 * Đánh dấu tất cả thông báo là đã đọc
 */
export async function markAllAsRead() {
  try {
    await axiosClient.patch('/notifications/read-all')
  } catch {
    // Backend chưa hỗ trợ endpoint PATCH, xử lý tiếp ở local mock
  }

  const list = getLocalNotifications()
  const updated = list.map((item) => ({ ...item, isRead: true }))
  saveLocalNotifications(updated)
  return updated
}

export default {
  getUnreadCount,
  getRecentNotifications,
  markAsRead,
  markAllAsRead,
}
