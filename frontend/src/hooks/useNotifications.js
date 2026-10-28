import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
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
 * Hook truy vấn số lượng thông báo chưa đọc từ server / mockData
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
    staleTime: 10000,
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
    staleTime: 10000,
    ...options,
  })
}

/**
 * Mutation đánh dấu một thông báo là đã đọc
 */
export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => notificationService.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
    },
  })
}

/**
 * Mutation đánh dấu tất cả thông báo là đã đọc
 */
export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => notificationService.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
    },
  })
}
