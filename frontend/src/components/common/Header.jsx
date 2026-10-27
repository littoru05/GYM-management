import { useState, useRef, useEffect } from 'react'
import { Dumbbell, LogOut, Bell, User, Clock, CheckCircle2, ChevronRight } from 'lucide-react'
import { useAuthStore } from '../../store/useAuthStore'
import { useLogout } from '../../hooks/useAuth'
import { useUnreadNotificationsCount, useRecentNotifications, formatBadgeCount } from '../../hooks/useNotifications'

/**
 * Component Header dùng chung cho cả Staff và Admin console
 *
 * @param {Object} props
 * @param {string} [props.title] - Tiêu đề hệ thống (ví dụ: 'PowerFit Gym · Admin' hoặc 'PowerFit Gym · Quầy')
 * @param {'ADMIN'|'STAFF'} [props.role] - Quyền hạn hiện tại
 * @param {number} [props.unreadCount] - Giá trị ghi đè unreadCount (nếu truyền vào sẽ ưu tiên sử dụng thay vì query)
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
  const dropdownRef = useRef(null)

  // Nếu prop unreadCount được truyền vào (kể cả 0), ưu tiên sử dụng; nếu không sẽ query từ API
  const { data: queriedCount = 0 } = useUnreadNotificationsCount({
    enabled: typeof propUnreadCount !== 'number',
  })
  const { data: recentNotifications = [], isLoading: isLoadingRecent } = useRecentNotifications(5, {
    enabled: isOpenDropdown,
  })

  const effectiveCount = typeof propUnreadCount === 'number' ? propUnreadCount : queriedCount
  const badgeText = formatBadgeCount(effectiveCount)

  // Đóng dropdown khi click ra ngoài
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

  // Xác định nhãn vai trò hiển thị
  const userRole = role || user?.role || (title.includes('Admin') ? 'ADMIN' : 'STAFF')
  const roleLabel = userRole === 'ADMIN' ? 'Quản trị viên' : 'Nhân viên quầy'
  const roleBadgeStyle =
    userRole === 'ADMIN'
      ? 'bg-purple-50 text-purple-700 border-purple-200'
      : 'bg-blue-50 text-blue-700 border-blue-200'

  const userInitial = user?.name ? user.name.charAt(0).toUpperCase() : 'U'

  return (
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

          {/* DROPDOWN DANH SÁCH THÔNG BÁO GẦN NHẤT (UX/UI Tiện ích) */}
          {isOpenDropdown && (
            <div
              id="notification-dropdown-panel"
              className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl border border-gray-100 bg-white shadow-xl ring-1 ring-black/5 animate-fade-in z-50 overflow-hidden"
            >
              {/* Header của Popover */}
              <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50/70 px-4 py-3">
                <div className="flex items-center gap-2">
                  <span className="font-semibold text-gray-900 text-sm">Thông báo mới</span>
                  {effectiveCount > 0 && (
                    <span className="rounded-full bg-red-100 px-2 py-0.5 text-[11px] font-semibold text-red-700">
                      {badgeText} chưa đọc
                    </span>
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => setIsOpenDropdown(false)}
                  className="text-xs text-gray-400 hover:text-gray-600 p-1"
                >
                  Đóng
                </button>
              </div>

              {/* Danh sách thông báo */}
              <div className="max-h-80 overflow-y-auto divide-y divide-gray-50">
                {isLoadingRecent ? (
                  <div className="p-6 text-center text-sm text-gray-400">
                    Đang tải thông báo...
                  </div>
                ) : recentNotifications.length === 0 ? (
                  <div className="p-6 text-center">
                    <div className="mx-auto mb-2 flex h-10 w-10 items-center justify-center rounded-full bg-green-50 text-green-600">
                      <CheckCircle2 size={20} />
                    </div>
                    <p className="text-sm font-medium text-gray-700">Tất cả đã được đọc</p>
                    <p className="text-xs text-gray-400 mt-0.5">Không có thông báo mới nào cần xử lý</p>
                  </div>
                ) : (
                  recentNotifications.map((item) => (
                    <div
                      key={item.id}
                      className={`p-3.5 hover:bg-gray-50/80 transition-colors flex items-start gap-3 ${
                        !item.isRead ? 'bg-primary-50/20' : ''
                      }`}
                    >
                      <div className="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary-100 text-primary-600">
                        <User size={14} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-1">
                          <p className="text-xs font-semibold text-gray-900 truncate">
                            {item.fullName || item.name || 'Khách hàng liên hệ'}
                          </p>
                          <span className="text-[10px] text-gray-400 shrink-0 flex items-center gap-0.5">
                            <Clock size={10} />
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleDateString('vi-VN', {
                                  hour: '2-digit',
                                  minute: '2-digit',
                                })
                              : 'Vừa xong'}
                          </span>
                        </div>
                        <p className="text-xs text-gray-600 mt-0.5 line-clamp-2">
                          {item.notes || item.message || `Số điện thoại: ${item.phoneNumber || 'N/A'}`}
                        </p>
                      </div>
                    </div>
                  ))
                )}
              </div>

              {/* Footer */}
              <div className="border-t border-gray-100 bg-gray-50/50 p-2 text-center">
                <span className="text-[11px] text-gray-500 font-medium inline-flex items-center gap-1">
                  PowerFit Notification Service
                  <ChevronRight size={12} />
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
  )
}

export default Header
