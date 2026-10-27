import assert from 'node:assert/strict'
import { formatBadgeCount } from './src/hooks/useNotifications.js'
import notificationService from './src/services/notificationService.js'

console.log('--- STARTING HEADER NOTIFICATION & BADGE UNIT TESTS ---')

// 1. Kiểm tra logic formatBadgeCount theo yêu cầu DoD:
// "Thêm Badge màu đỏ nổi bật đè lên góc phải trên của icon chuông khi unreadCount > 0.
//  Nếu unreadCount > 99, hiển thị 99+. Nếu bằng 0, ẩn badge."

// Case 1: unreadCount = 0 -> ẩn badge (trả về null)
assert.equal(formatBadgeCount(0), null, 'unreadCount = 0 phải trả về null để ẩn badge')

// Case 2: unreadCount âm hoặc giá trị rỗng -> ẩn badge
assert.equal(formatBadgeCount(-1), null, 'unreadCount < 0 phải trả về null')
assert.equal(formatBadgeCount(null), null, 'unreadCount = null phải trả về null')
assert.equal(formatBadgeCount(undefined), null, 'unreadCount = undefined phải trả về null')
assert.equal(formatBadgeCount(''), null, 'unreadCount chuỗi rỗng phải trả về null')
assert.equal(formatBadgeCount('abc'), null, 'unreadCount không hợp lệ phải trả về null')
console.log('✓ PASS: formatBadgeCount ẩn badge chính xác khi unreadCount <= 0 hoặc không hợp lệ')

// Case 3: 0 < unreadCount <= 99 -> hiển thị chính xác số dạng chuỗi
assert.equal(formatBadgeCount(1), '1', 'unreadCount = 1 phải hiển thị "1"')
assert.equal(formatBadgeCount(9), '9', 'unreadCount = 9 phải hiển thị "9"')
assert.equal(formatBadgeCount(15), '15', 'unreadCount = 15 phải hiển thị "15"')
assert.equal(formatBadgeCount(99), '99', 'unreadCount = 99 phải hiển thị "99"')
assert.equal(formatBadgeCount('25'), '25', 'unreadCount dạng chuỗi "25" phải ép kiểu hợp lệ thành "25"')
console.log('✓ PASS: formatBadgeCount hiển thị chính xác giá trị số trong khoảng 1 - 99')

// Case 4: unreadCount > 99 -> hiển thị "99+"
assert.equal(formatBadgeCount(100), '99+', 'unreadCount = 100 phải hiển thị "99+"')
assert.equal(formatBadgeCount(150), '99+', 'unreadCount = 150 phải hiển thị "99+"')
assert.equal(formatBadgeCount(999), '99+', 'unreadCount = 999 phải hiển thị "99+"')
assert.equal(formatBadgeCount(10000), '99+', 'unreadCount lớn phải hiển thị "99+"')
console.log('✓ PASS: formatBadgeCount hiển thị "99+" khi unreadCount > 99')

// 2. Kiểm tra notificationService fallback an toàn
const countFallback = await notificationService.getUnreadCount()
assert.ok(typeof countFallback.unreadCount === 'number', 'notificationService.getUnreadCount phải luôn trả về object có thuộc tính unreadCount dạng number')
console.log('✓ PASS: notificationService.getUnreadCount trả về fallback an toàn khi backend offline')

const recentFallback = await notificationService.getRecentNotifications(5)
assert.ok(Array.isArray(recentFallback), 'notificationService.getRecentNotifications phải luôn trả về mảng danh sách')
console.log('✓ PASS: notificationService.getRecentNotifications trả về mảng fallback an toàn khi backend offline')

console.log('\n--- TẤT CẢ UNIT TESTS NOTIFICATION BADGE ĐÃ PASS XUẤT SẮC! ---')
