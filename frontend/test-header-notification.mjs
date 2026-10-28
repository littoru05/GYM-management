import assert from 'node:assert/strict'
import { formatBadgeCount } from './src/hooks/useNotifications.js'
import notificationService from './src/services/notificationService.js'
import { formatRelativeTime } from './src/utils/formatters.js'

console.log('--- STARTING HEADER NOTIFICATION, DROPDOWN & MODAL UNIT TESTS ---')

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

// 3. Kiểm tra notificationService với mockData & Đánh dấu đã đọc
const initialCount = await notificationService.getUnreadCount()
assert.ok(typeof initialCount.unreadCount === 'number', 'unreadCount phải là dạng số')
console.log(`✓ PASS: notificationService.getUnreadCount trả về: ${initialCount.unreadCount}`)

const recentList = await notificationService.getRecentNotifications(5)
assert.ok(Array.isArray(recentList), 'recentList phải là một mảng')
assert.ok(recentList.length > 0, 'recentList phải có dữ liệu mock ban đầu')

// Kiểm tra các trường dữ liệu bắt buộc của từng thông báo
for (const item of recentList) {
  assert.ok(item.id, 'Thông báo phải có id')
  assert.ok(item.name, 'Thông báo phải có tên khách hàng')
  assert.ok(item.phone, 'Thông báo phải có số điện thoại')
  assert.ok(item.message, 'Thông báo phải có nội dung lời nhắn')
  assert.ok(item.createdAt, 'Thông báo phải có thời gian gửi createdAt')
  assert.equal(typeof item.isRead, 'boolean', 'Trạng thái isRead phải là boolean')
}
console.log('✓ PASS: Mỗi thông báo chứa đầy đủ: Tên khách hàng, SĐT, Lời nhắn, Thời gian và Trạng thái đọc')

// 4. Kiểm tra chức năng Đánh dấu đã đọc (markAsRead)
const unreadItem = recentList.find((item) => !item.isRead)
if (unreadItem) {
  const updatedItem = await notificationService.markAsRead(unreadItem.id)
  assert.equal(updatedItem.isRead, true, 'Sau khi markAsRead, isRead phải chuyển thành true')
  console.log(`✓ PASS: markAsRead(${unreadItem.id}) chuyển trạng thái thành Đã đọc thành công`)
}

// 5. Kiểm tra chức năng Đánh dấu tất cả đã đọc (markAllAsRead)
const allReadList = await notificationService.markAllAsRead()
for (const item of allReadList) {
  assert.equal(item.isRead, true, 'Sau khi markAllAsRead, tất cả item phải có isRead = true')
}
const finalCount = await notificationService.getUnreadCount()
assert.equal(finalCount.unreadCount, 0, 'Sau khi markAllAsRead, unreadCount phải bằng 0')
console.log('✓ PASS: markAllAsRead chuyển toàn bộ thông báo thành Đã đọc và unreadCount = 0')

console.log('\n--- TẤT CẢ UNIT TESTS NOTIFICATION BADGE, DROPDOWN & MODAL ĐÃ PASS 100%! ---')
