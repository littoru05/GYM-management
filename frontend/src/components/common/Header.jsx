import { useState, useRef, useEffect } from 'react'
import { Dumbbell, LogOut, Bell, User, Phone, Clock, CheckCircle2, CheckCheck } from 'lucide-react'
import { useAuthStore } from '../../store/useAuthStore.js'
import { useLogout } from '../../hooks/useAuth.js'
import {
  useUnreadNotificationsCount,
  useRecentNotifications,
  useMarkNotificationAsRead,
  useMarkAllNotificationsAsRead,
  formatBadgeCount,
} from '../../hooks/useNotifications.js'
import NotificationDetailModal from './NotificationDetailModal.jsx'
import { formatRelativeTime } from '../../utils/formatters.js'

/**
 * Component Header dùng chung cho cả Staff và Admin console
 * Tích hợp chuông thông báo, badge đè góc trên phải, dropdown phân biệt Đã đọc / Chưa đọc,
 * và modal xem đầy đủ chi tiết liên hệ và lời nhắn của khách hàng.
 *
 * @param {Object} props
 * @param {string} [props.title] - Tiêu đề hệ thống (ví dụ: 'PowerFit Gym · Admin' hoặc 'PowerFit Gym · Quầy')
 * @param {'ADMIN'|'STAFF'} [props.role] - Quyền hạn hiện tại
 * @param {number} [props.unreadCount] - Giá trị ghi đè unreadCount (nếu truyền vào sẽ ưu tiên sử dụng)
 * @param {Function} [props.onNotificationClick] - Callback khi bấm vào icon chuông
 * @param {string} [props.className] - CSS class bổ sung
 */
function Header({
  title = 'PowerFit Gym',
  role,
  unreadCount: propUnreadCount,
  onNotificationClick,
  className = '',
}) {
  const user = useAuthStore((state) => state.user)
  const handleLogout = useLogout()

  const [isOpenDropdown, setIsOpenDropdown] = useState(false)
  const [selectedNotification, setSelectedNotification] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const dropdownRef = useRef(null)

  // Truy vấn số lượng chưa đọc & danh sách thông báo
  const { data: queriedCount = 0 } = useUnreadNotificationsCount({
    enabled: typeof propUnreadCount !== 'number',
  })
  const { data: recentNotifications = [], isLoading: isLoadingRecent } = useRecentNotifications(8, {
    enabled: isOpenDropdown || isModalOpen,
  })

  const markAsReadMutation = useMarkNotificationAsRead()
  const markAllAsReadMutation = useMarkAllNotificationsAsRead()

  const effectiveCount = typeof propUnreadCount === 'number' ? propUnreadCount : queriedCount
  const badgeText = formatBadgeCount(effectiveCount)

  // Đóng dropdown khi click outside hoặc ấn Escape
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpenDropdown(false)
      }
    }
    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        setIsOpenDropdown(false)
      }
    }
    if (isOpenDropdown) {
      document.addEventListener('mousedown', handleClickOutside)
      document.addEventListener('keydown', handleKeyDown)
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpenDropdown])

  const handleToggleBell = (e) => {
    if (onNotificationClick) {
      onNotificationClick(e)
    }
    setIsOpenDropdown((prev) => !prev)
  }

  // Khi click vào một thông báo cụ thể trong dropdown
  const handleSelectNotification = (item) => {
    setSelectedNotification(item)
    // Tự động đánh dấu đã đọc khi xem chi tiết
    if (!item.isRead) {
      markAsReadMutation.mutate(item.id)
    }
    setIsOpenDropdown(false)
    setIsModalOpen(true)
  }

  // Đánh dấu tất cả là đã đọc
  const handleMarkAllRead = (e) => {
    e.stopPropagation()
    markAllAsReadMutation.mutate()
  }

  // Nhãn vai trò hiển thị
  const userRole = role || user?.role || (title.includes('Admin') ? 'ADMIN' : 'STAFF')
  const roleLabel = userRole === 'ADMIN' ? 'Quản trị viên' : 'Nhân viên quầy'
  const roleBadgeStyle =
    userRole === 'ADMIN'
      ? 'bg-purple-50 text-purple-700 border-purple-200'
      : 'bg-blue-50 text-blue-700 border-blue-200'

  const userInitial = user?.name ? user.name.charAt(0).toUpperCase() : 'U'

  return (
    <>
      <header
        id="app-header"
        className={`relative flex h-16 w-full items-center justify-between border-b border-gray-100 bg-white px-4 sm:px-6 z-30 transition-shadow ${className}`}
      >
        {/* KHỐI TRÁI: Logo + Tiêu đề hệ thống */}
        <div className="flex items-center gap-2.5 sm:gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-primary-500 text-white shadow-sm shadow-primary-500/20">
            <Dumbbell size={18} />
          </div>
          <div className="flex flex-col">
            <span className="text-base sm:text-lg font-bold text-gray-900 tracking-tight leading-tight">
              {title}
            </span>
            <span className="text-[11px] font-medium text-gray-400 hidden sm:block">
              Hệ thống Quản lý Phòng Gym Cao Cấp
            </span>
          </div>
        </div>

        {/* KHỐI PHẢI: Chuông thông báo + Thông tin người dùng + Đăng xuất */}
        <div className="flex items-center gap-2 sm:gap-4">
          {/* ICON CHUÔNG THÔNG BÁO VỚI BADGE ĐỎ */}
          <div className="relative" ref={dropdownRef}>
            <button
              type="button"
              id="header-notification-bell-btn"
              data-testid="notification-bell-btn"
              aria-label="Thông báo"
              title={effectiveCount > 0 ? `${effectiveCount} thông báo chưa đọc` : 'Không có thông báo mới'}
              onClick={handleToggleBell}
              className={`relative flex h-10 w-10 items-center justify-center rounded-xl transition-all duration-200 ${
                isOpenDropdown
                  ? 'bg-gray-100 text-primary-600 ring-2 ring-primary-500/20'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              }`}
            >
              <Bell size={20} className="transition-transform duration-200 hover:scale-105" />

              {/* BADGE MÀU ĐỎ NỔI BẬT ĐÈ LÊN GÓC PHẢI TRÊN KHI unreadCount > 0 */}
              {effectiveCount > 0 && badgeText && (
                <span
                  id="notification-badge"
                  data-testid="notification-badge"
                  aria-label={`${badgeText} thông báo chưa đọc`}
                  className="absolute -top-1 -right-1 flex h-5 min-w-[20px] items-center justify-center rounded-full bg-red-600 px-1.5 text-[11px] font-bold leading-none text-white shadow-sm ring-2 ring-white animate-fade-in pointer-events-none select-none"
                >
                  {badgeText}
                </span>
              )}
            </button>

            {/* DROPDOWN MENU DANH SÁCH THÔNG BÁO MỚI NHẤT */}
            {isOpenDropdown && (
              <div
                id="notification-dropdown-panel"
                data-testid="notification-dropdown-panel"
                className="absolute right-0 mt-2 w-84 sm:w-96 rounded-2xl border border-gray-100 bg-white shadow-xl ring-1 ring-black/5 animate-fade-in z-50 overflow-hidden"
              >
                {/* Header Dropdown */}
                <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50/70 px-4 py-3">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-900 text-sm">Thông báo khách hàng</span>
                    {effectiveCount > 0 && (
                      <span className="rounded-full bg-red-100 px-2 py-0.5 text-[11px] font-bold text-red-700">
                        {badgeText} chưa đọc
                      </span>
                    )}
                  </div>
                  {effectiveCount > 0 && (
                    <button
                      type="button"
                      onClick={handleMarkAllRead}
                      data-testid="mark-all-read-btn"
                      className="inline-flex items-center gap-1 text-[11px] font-medium text-primary-600 hover:text-primary-700 hover:underline"
                    >
                      <CheckCheck size={13} />
                      Đã đọc tất cả
                    </button>
                  )}
                </div>

                {/* Danh sách thông báo */}
                <div
                  id="notification-items-list"
                  className="max-h-88 overflow-y-auto divide-y divide-gray-100"
                >
                  {isLoadingRecent ? (
                    <div className="p-6 text-center text-sm text-gray-400">
                      Đang tải danh sách thông báo...
                    </div>
                  ) : recentNotifications.length === 0 ? (
                    <div className="p-8 text-center">
                      <div className="mx-auto mb-2 flex h-10 w-10 items-center justify-center rounded-full bg-green-50 text-green-600">
                        <CheckCircle2 size={20} />
                      </div>
                      <p className="text-sm font-medium text-gray-700">Hộp thư trống</p>
                      <p className="text-xs text-gray-400 mt-0.5">
                        Chưa có yêu cầu liên hệ mới nào
                      </p>
                    </div>
                  ) : (
                    recentNotifications.map((item) => {
                      const isUnread = !item.isRead
                      const name = item.name || item.fullName || 'Khách hàng liên hệ'
                      const phone = item.phone || item.phoneNumber || '—'
                      const relativeTime = formatRelativeTime(item.createdAt)

                      return (
                        <div
                          key={item.id}
                          role="button"
                          tabIndex={0}
                          data-testid={`notification-item-${item.id}`}
                          onClick={() => handleSelectNotification(item)}
                          onKeyDown={(e) => {
                            if (e.key === 'Enter' || e.key === ' ') {
                              handleSelectNotification(item)
                            }
                          }}
                          className={`group relative flex cursor-pointer items-start gap-3 p-3.5 transition-colors ${
                            isUnread
                              ? 'bg-primary-50/30 hover:bg-primary-50/60 border-l-4 border-primary-500'
                              : 'bg-white hover:bg-gray-50 border-l-4 border-transparent'
                          }`}
                        >
                          {/* Avatar icon */}
                          <div
                            className={`mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                              isUnread
                                ? 'bg-primary-100 text-primary-600 ring-2 ring-primary-200'
                                : 'bg-gray-100 text-gray-500'
                            }`}
                          >
                            <User size={14} />
                          </div>

                          {/* Nội dung item */}
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between gap-1 mb-0.5">
                              {/* Tên khách hàng */}
                              <span
                                className={`text-xs truncate ${
                                  isUnread ? 'font-bold text-gray-900' : 'font-medium text-gray-700'
                                }`}
                              >
                                {name}
                              </span>

                              {/* Trạng thái Chưa đọc / Đã đọc */}
                              {isUnread ? (
                                <span
                                  data-testid={`badge-unread-${item.id}`}
                                  className="shrink-0 inline-flex items-center gap-1 rounded bg-red-100 px-1.5 py-0.5 text-[10px] font-bold text-red-700"
                                >
                                  <span className="h-1.5 w-1.5 rounded-full bg-red-500 animate-pulse" />
                                  Chưa đọc
                                </span>
                              ) : (
                                <span
                                  data-testid={`badge-read-${item.id}`}
                                  className="shrink-0 rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-medium text-gray-500"
                                >
                                  Đã đọc
                                </span>
                              )}
                            </div>

                            {/* Số điện thoại */}
                            <div className="flex items-center gap-1 text-[11px] font-mono text-gray-600 mb-1">
                              <Phone size={11} className="text-gray-400" />
                              <span>{phone}</span>
                            </div>

                            {/* Đoạn trích lời nhắn */}
                            <p className="text-xs text-gray-500 line-clamp-1 mb-1 leading-relaxed">
                              {item.message || item.notes || 'Không có lời nhắn.'}
                            </p>

                            {/* Thời gian gửi dạng relative (x phút trước) */}
                            <div className="flex items-center gap-1 text-[10px] text-gray-400 font-medium">
                              <Clock size={10} className="text-gray-400" />
                              <span>{relativeTime}</span>
                            </div>
                          </div>
                        </div>
                      )
                    })
                  )}
                </div>

                {/* Footer Dropdown */}
                <div className="border-t border-gray-100 bg-gray-50/50 px-4 py-2 text-center">
                  <span className="text-[11px] text-gray-500">
                    Bấm vào thông báo để xem đầy đủ nội dung lời nhắn
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* PHÂN CÁCH NHẸ GIỮA CÁC KHỐI */}
          <div className="h-6 w-px bg-gray-200 hidden sm:block" />

          {/* THÔNG TIN NGƯỜI DÙNG */}
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-tr from-primary-600 to-primary-400 text-sm font-bold text-white shadow-sm ring-2 ring-primary-100">
              {userInitial}
            </div>
            <div className="hidden md:flex flex-col">
              <span className="text-sm font-semibold text-gray-900 leading-snug truncate max-w-[150px]">
                {user?.name || user?.username || 'Người dùng'}
              </span>
              <span
                className={`inline-block self-start rounded border px-1.5 py-0.2 text-[10px] font-semibold leading-tight ${roleBadgeStyle}`}
              >
                {roleLabel}
              </span>
            </div>
          </div>

          {/* NÚT ĐĂNG XUẤT */}
          <button
            type="button"
            onClick={handleLogout}
            id="header-logout-btn"
            aria-label="Đăng xuất"
            title="Đăng xuất khỏi hệ thống"
            className="flex items-center gap-1.5 rounded-xl px-2.5 py-2 sm:px-3 text-sm font-medium text-gray-600 hover:bg-red-50 hover:text-red-600 transition-colors"
          >
            <LogOut size={16} />
            <span className="hidden sm:inline">Đăng xuất</span>
          </button>
        </div>
      </header>

      {/* MODAL XEM ĐẦY ĐỦ NỘI DUNG LỜI NHẮN & THÔNG TIN LIÊN HỆ */}
      <NotificationDetailModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        notification={selectedNotification}
        onMarkAsRead={(id) => {
          markAsReadMutation.mutate(id)
          // Cập nhật state hiển thị ngay trong modal đang mở
          setSelectedNotification((prev) => (prev ? { ...prev, isRead: true } : null))
        }}
      />
    </>
  )
}

export default Header
