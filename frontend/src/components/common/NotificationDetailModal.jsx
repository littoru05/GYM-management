import { useState } from 'react'
import { Phone, Mail, Clock, CheckCircle, Copy, Check, MessageSquare, ExternalLink } from 'lucide-react'
import Modal from './Modal.jsx'
import { formatDateTime, formatRelativeTime } from '../../utils/formatters.js'
import { showSuccessToast } from '../../utils/toastService.js'

/**
 * Modal hiển thị chi tiết thông tin liên hệ và nội dung lời nhắn của khách hàng
 *
 * @param {Object} props
 * @param {boolean} props.open - Trạng thái hiển thị modal
 * @param {Function} props.onClose - Hàm đóng modal
 * @param {Object} props.notification - Dữ liệu thông báo cần xem
 * @param {Function} [props.onMarkAsRead] - Callback đánh dấu đã đọc
 */
function NotificationDetailModal({ open, onClose, notification, onMarkAsRead }) {
  const [copied, setCopied] = useState(false)

  if (!notification) return null

  const customerName = notification.name || notification.fullName || 'Khách hàng liên hệ'
  const customerPhone = notification.phone || notification.phoneNumber || '—'
  const customerEmail = notification.email || '—'
  const customerMessage = notification.message || notification.notes || 'Không có nội dung lời nhắn.'
  const isRead = Boolean(notification.isRead)

  const handleCopyPhone = () => {
    if (customerPhone && customerPhone !== '—') {
      navigator.clipboard?.writeText(customerPhone)
      setCopied(true)
      showSuccessToast(`Đã sao chép số điện thoại: ${customerPhone}`)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  const handleMarkRead = () => {
    if (onMarkAsRead && !isRead) {
      onMarkAsRead(notification.id)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Chi tiết liên hệ khách hàng" size="md">
      <div className="space-y-5">
        {/* THÔNG TIN CHÍNH CỦA KHÁCH HÀNG & TRẠNG THÁI */}
        <div className="flex items-start justify-between gap-3 border-b border-gray-100 pb-4">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-tr from-primary-600 to-primary-400 text-lg font-bold text-white shadow-sm">
              {customerName.charAt(0).toUpperCase()}
            </div>
            <div>
              <h3 className="text-base font-bold text-gray-900 leading-tight">{customerName}</h3>
              <p className="text-xs text-gray-400 mt-0.5 flex items-center gap-1">
                <Clock size={12} />
                <span>{formatDateTime(notification.createdAt)}</span>
                <span className="font-medium text-primary-600">
                  ({formatRelativeTime(notification.createdAt)})
                </span>
              </p>
            </div>
          </div>

          {/* BADGE TRẠNG THÁI ĐÃ ĐỌC / CHƯA ĐỌC */}
          <div>
            {isRead ? (
              <span
                data-testid="detail-status-read"
                className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200"
              >
                <CheckCircle size={12} />
                Đã đọc
              </span>
            ) : (
              <span
                data-testid="detail-status-unread"
                className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700 border border-red-200"
              >
                <span className="h-1.5 w-1.5 rounded-full bg-red-500 animate-pulse" />
                Chưa đọc
              </span>
            )}
          </div>
        </div>

        {/* THÔNG TIN LIÊN HỆ */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* Số điện thoại */}
          <div className="rounded-xl border border-gray-100 bg-gray-50/60 p-3">
            <div className="flex items-center justify-between text-xs text-gray-500 mb-1">
              <span className="flex items-center gap-1 font-medium">
                <Phone size={13} className="text-primary-500" />
                Số điện thoại
              </span>
              {customerPhone !== '—' && (
                <button
                  type="button"
                  onClick={handleCopyPhone}
                  className="flex items-center gap-1 text-[11px] font-semibold text-primary-600 hover:text-primary-700"
                  title="Sao chép số điện thoại"
                >
                  {copied ? <Check size={12} /> : <Copy size={12} />}
                  <span>{copied ? 'Đã chép' : 'Sao chép'}</span>
                </button>
              )}
            </div>
            <p className="text-sm font-bold text-gray-900 tracking-wide font-mono">
              {customerPhone}
            </p>
          </div>

          {/* Email */}
          <div className="rounded-xl border border-gray-100 bg-gray-50/60 p-3">
            <div className="flex items-center text-xs text-gray-500 mb-1 font-medium">
              <Mail size={13} className="text-primary-500 mr-1" />
              Địa chỉ Email
            </div>
            <p className="text-sm font-medium text-gray-900 truncate" title={customerEmail}>
              {customerEmail}
            </p>
          </div>
        </div>

        {/* NỘI DUNG LỜI NHẮN CHI TIẾT */}
        <div>
          <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-700 mb-1.5">
            <MessageSquare size={14} className="text-primary-500" />
            Nội dung lời nhắn từ khách hàng:
          </div>
          <div className="rounded-xl border border-gray-200 bg-gray-50/80 p-4 text-sm text-gray-800 leading-relaxed whitespace-pre-wrap min-h-[90px]">
            {customerMessage}
          </div>
        </div>

        {/* NÚT THAO TÁC / HÀNH ĐỘNG */}
        <div className="flex items-center justify-between pt-3 border-t border-gray-100">
          <div>
            {!isRead && (
              <button
                type="button"
                onClick={handleMarkRead}
                data-testid="modal-mark-read-btn"
                className="inline-flex items-center gap-1.5 rounded-lg bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700 hover:bg-emerald-100 transition-colors"
              >
                <CheckCircle size={14} />
                Đánh dấu đã đọc
              </button>
            )}
          </div>

          <div className="flex items-center gap-2">
            {customerPhone !== '—' && (
              <a
                href={`tel:${customerPhone}`}
                className="inline-flex items-center gap-1 rounded-lg border border-primary-500 px-3 py-2 text-xs font-semibold text-primary-600 hover:bg-primary-50 transition-colors"
              >
                <Phone size={13} />
                Gọi ngay
                <ExternalLink size={11} />
              </a>
            )}
            <button
              type="button"
              onClick={onClose}
              data-testid="modal-close-btn"
              className="rounded-lg bg-gray-100 px-4 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-200 transition-colors"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </Modal>
  )
}

export default NotificationDetailModal
