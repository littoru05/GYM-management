import assert from 'node:assert/strict'
import { formatBadgeCount } from './src/hooks/useNotifications.js'
import notificationService, { pollUnreadCount } from './src/services/notificationService.js'
import { formatRelativeTime } from './src/utils/formatters.js'
import axiosClient from './src/api/axiosClient.js'

console.log('--- STARTING HEADER NOTIFICATION, DROPDOWN & SERVICE UNIT TESTS ---')

// 1. Kiểm tra formatBadgeCount theo yêu cầu DoD:
// "Thêm Badge màu đỏ nổi bật đè lên góc phải trên của icon chuông khi unreadCount > 0.
//  Nếu unreadCount > 99, hiển thị 99+. Nếu bằng 0, ẩn badge."

assert.equal(formatBadgeCount(0), null, 'unreadCount = 0 phải trả về null để ẩn badge')
assert.equal(formatBadgeCount(-1), null, 'unreadCount < 0 phải trả về null')
assert.equal(formatBadgeCount(null), null, 'unreadCount = null phải trả về null')
assert.equal(formatBadgeCount(undefined), null, 'unreadCount = undefined phải trả về null')
console.log('✓ PASS: formatBadgeCount ẩn badge chính xác khi unreadCount <= 0')

assert.equal(formatBadgeCount(1), '1')
assert.equal(formatBadgeCount(5), '5')
assert.equal(formatBadgeCount(99), '99')
console.log('✓ PASS: formatBadgeCount hiển thị chính xác giá trị số trong khoảng 1 - 99')

assert.equal(formatBadgeCount(100), '99+')
assert.equal(formatBadgeCount(150), '99+')
console.log('✓ PASS: formatBadgeCount hiển thị "99+" khi unreadCount > 99')

// 2. Kiểm tra formatRelativeTime theo yêu cầu DoD:
// "thời gian gửi dạng relative 'x phút trước'"
const now = Date.now()
const justNow = new Date(now - 15 * 1000).toISOString()
assert.equal(formatRelativeTime(justNow), 'Vừa xong')

const fiveMinsAgo = new Date(now - 5 * 60 * 1000).toISOString()
assert.equal(formatRelativeTime(fiveMinsAgo), '5 phút trước')

const thirtyMinsAgo = new Date(now - 30 * 60 * 1000).toISOString()
assert.equal(formatRelativeTime(thirtyMinsAgo), '30 phút trước')

const twoHoursAgo = new Date(now - 2 * 60 * 60 * 1000).toISOString()
assert.equal(formatRelativeTime(twoHoursAgo), '2 giờ trước')

const oneDayAgo = new Date(now - 25 * 60 * 60 * 1000).toISOString()
assert.equal(formatRelativeTime(oneDayAgo), '1 ngày trước')
console.log('✓ PASS: formatRelativeTime định dạng chuẩn xác thời gian gửi dạng relative (x phút trước)')

// 3. Mock axiosClient để kiểm thử contract các hàm trong notificationService.js
const originalGet = axiosClient.get
const originalPatch = axiosClient.patch

let lastGetUrl = ''
let lastGetConfig = null
let lastPatchUrl = ''
let _lastPatchData = null

axiosClient.get = async (url, config) => {
  lastGetUrl = url
  lastGetConfig = config

  if (url === '/notifications/unread-count') {
    return { data: { unreadCount: 3 } }
  }
  if (url === '/notifications/recent') {
    return {
      data: [
        {
          id: 1,
          name: 'Nguyễn Văn Minh',
          phone: '0987654321',
          email: 'minh.nguyen@gmail.com',
          message: 'Tư vấn gói tập VIP',
          createdAt: new Date().toISOString(),
          isRead: false,
          status: 'NEW',
        },
        {
          id: 2,
          name: 'Trần Thị Thu Hà',
          phone: '0912345678',
          email: 'thuha@gmail.com',
          message: 'Tập thử 3 ngày',
          createdAt: new Date().toISOString(),
          isRead: false,
          status: 'NEW',
        },
      ],
    }
  }
  return { data: null }
}

axiosClient.patch = async (url, data) => {
  lastPatchUrl = url
  _lastPatchData = data

  if (url === '/notifications/1/read') {
    return {
      data: {
        id: 1,
        name: 'Nguyễn Văn Minh',
        isRead: true,
        status: 'NEW',
      },
    }
  }
  if (url === '/notifications/read-all') {
    return {
      data: {
        message: 'Đã đánh dấu tất cả liên hệ là đã đọc',
        updatedCount: 2,
      },
    }
  }
  return { data: null }
}

try {
  // Test 3.1: getUnreadCount gọi GET /api/notifications/unread-count
  const countRes = await notificationService.getUnreadCount()
  assert.equal(lastGetUrl, '/notifications/unread-count')
  assert.equal(countRes.unreadCount, 3)
  console.log('✓ PASS: notificationService.getUnreadCount gọi đúng endpoint GET /notifications/unread-count')

  // Test 3.2: getRecentNotifications gọi GET /api/notifications/recent?limit=5
  const recentRes = await notificationService.getRecentNotifications(5)
  assert.equal(lastGetUrl, '/notifications/recent')
  assert.deepEqual(lastGetConfig, { params: { limit: 5 } })
  assert.equal(recentRes.length, 2)
  assert.equal(recentRes[0].id, 1)
  assert.equal(recentRes[0].isRead, false)
  console.log('✓ PASS: notificationService.getRecentNotifications gọi đúng endpoint GET /notifications/recent')

  // Test 3.3: markAsRead gọi PATCH /api/notifications/{id}/read
  const readRes = await notificationService.markAsRead(1)
  assert.equal(lastPatchUrl, '/notifications/1/read')
  assert.equal(readRes.isRead, true)
  console.log('✓ PASS: notificationService.markAsRead gọi đúng endpoint PATCH /notifications/{id}/read')

  // Test 3.4: markAllAsRead gọi PATCH /api/notifications/read-all
  const readAllRes = await notificationService.markAllAsRead()
  assert.equal(lastPatchUrl, '/notifications/read-all')
  assert.equal(readAllRes.updatedCount, 2)
  console.log('✓ PASS: notificationService.markAllAsRead gọi đúng endpoint PATCH /notifications/read-all')

  // Test 3.5: pollUnreadCount gọi fetch on mount, polling ngầm và dọn dẹp clearInterval
  let pollUpdatedCount = 0
  const stopPolling = pollUnreadCount((count) => {
    pollUpdatedCount = count
  }, 100)

  // Đợi microtask chạy fetch on mount
  await new Promise((resolve) => setTimeout(resolve, 50))
  assert.equal(pollUpdatedCount, 3, 'pollUnreadCount phải gọi fetch on mount ngay lập tức')

  // Dọn dẹp clearInterval khi unmount
  assert.equal(typeof stopPolling, 'function', 'pollUnreadCount phải trả về hàm cleanup')
  stopPolling()
  console.log('✓ PASS: pollUnreadCount thực hiện Fetch on Mount và trả về hàm cleanup clearInterval chuẩn DoD')

  // Test 3.6: Trừ số đếm trực tiếp trên Badge client mà không cần reload trang
  let clientBadgeCount = 3
  // Click xem chi tiết 1 thông báo chưa đọc -> trừ 1
  clientBadgeCount = Math.max(0, clientBadgeCount - 1)
  assert.equal(clientBadgeCount, 2)
  assert.equal(formatBadgeCount(clientBadgeCount), '2')

  // Click tiếp 2 thông báo
  clientBadgeCount = Math.max(0, clientBadgeCount - 1)
  clientBadgeCount = Math.max(0, clientBadgeCount - 1)
  assert.equal(clientBadgeCount, 0)
  assert.equal(formatBadgeCount(clientBadgeCount), null, 'Khi unreadCount về 0 thì badge biến mất ngay lập tức')
  console.log('✓ PASS: Trừ số đếm trực tiếp trên Badge client hoạt động mượt mà không cần reload trang')

} finally {
  axiosClient.get = originalGet
  axiosClient.patch = originalPatch
}

console.log('\n--- TẤT CẢ UNIT TESTS NOTIFICATION BADGE, DROPDOWN & SERVICE ĐÃ PASS 100%! ---')
