import { useQuery } from '@tanstack/react-query'
import * as notificationService from '../services/notificationService.js'

/**
 * Định dạng hiển thị badge theo đúng yêu cầu nghiệp vụ:
 * - unreadCount <= 0: null (ẩn badge)
 * - unreadCount > 99: "99+"
 * - 0 < unreadCount <= 99: hiển thị số chính xác dạng chuỗi
 */
export function formatBadgeCount(count) {
  const num = Number(count)
  if (isNaN(num) || num <= 0) {
    return null
  }
  if (num > 99) {
    return '99+'
  }
  return String(num)
}

/**
 * Hook truy vấn số lượng thông báo chưa đọc từ server
 * Tự động đồng bộ mỗi 30 giây và khi focus lại cửa sổ trình duyệt
 */
export function useUnreadNotificationsCount(options = {}) {
  return useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => {
      const res = await notificationService.getUnreadCount()
      return typeof res?.unreadCount === 'number' ? res.unreadCount : 0
    },
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
    staleTime: 15000,
    ...options,
  })
}

/**
 * Hook truy vấn danh sách thông báo mới nhất cho popover/dropdown
 */
export function useRecentNotifications(limit = 5, options = {}) {
  return useQuery({
    queryKey: ['notifications', 'recent', limit],
    queryFn: () => notificationService.getRecentNotifications(limit),
    staleTime: 15000,
    ...options,
  })
}
