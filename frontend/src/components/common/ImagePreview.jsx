import { useState, useEffect } from 'react'
import { Image as ImageIcon, ImageOff, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react'

const API_ORIGIN = (import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/api\/?$/, '')

function resolvePreviewUrl(rawUrl) {
  if (!rawUrl) return ''
  const trimmed = String(rawUrl).trim()
  if (trimmed.startsWith('/uploads/')) {
    return `${API_ORIGIN}${trimmed}`
  }
  return trimmed
}

/**
 * ImagePreview component:
 * Tự động hiển thị khung xem trước ảnh từ File tải lên hoặc URL.
 * Hỗ trợ các định dạng: blob:, data:image/, http://, https://, /uploads/...
 */
function ImagePreview({
  url,
  alt = 'Ảnh xem trước',
  className = '',
  heightClass = 'h-40',
}) {
  const displayUrl = resolvePreviewUrl(url)
  const isValidUrl = /^(https?:\/\/|blob:|data:image\/|\/)/i.test(displayUrl)
  const [loadState, setLoadState] = useState({ url: '', state: 'idle' })

  // Trạng thái được suy luận trực tiếp để tránh gọi setState đồng bộ trong effect
  let status = 'idle'
  if (!displayUrl) {
    status = 'idle'
  } else if (!isValidUrl) {
    status = 'invalid'
  } else if (loadState.url === displayUrl) {
    status = loadState.state
  } else {
    status = 'loading'
  }

  useEffect(() => {
    if (!displayUrl || !isValidUrl) return

    let active = true
    const img = new Image()
    img.src = displayUrl
    img.onload = () => {
      if (active) setLoadState({ url: displayUrl, state: 'success' })
    }
    img.onerror = () => {
      if (active) setLoadState({ url: displayUrl, state: 'error' })
    }

    return () => {
      active = false
      img.onload = null
      img.onerror = null
    }
  }, [displayUrl, isValidUrl])

  return (
    <div className={`space-y-1.5 ${className}`}>
      <div className="flex items-center justify-between text-xs">
        <span className="font-semibold text-gray-700">Khung xem trước ảnh</span>
        {status === 'success' && (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-emerald-600">
            <CheckCircle2 size={13} />
            <span>Ảnh hợp lệ</span>
          </span>
        )}
        {status === 'loading' && (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-primary-600">
            <Loader2 size={13} className="animate-spin" />
            <span>Đang tải ảnh...</span>
          </span>
        )}
        {status === 'error' && (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-red-500">
            <AlertCircle size={13} />
            <span>Không thể tải ảnh</span>
          </span>
        )}
      </div>

      <div
        data-testid="image-preview-container"
        className={`relative flex w-full ${heightClass} items-center justify-center overflow-hidden rounded-xl border bg-gray-50 transition-all ${
          status === 'success'
            ? 'border-emerald-300 ring-2 ring-emerald-50 shadow-xs'
            : status === 'error'
            ? 'border-red-300 bg-red-50/30'
            : 'border-dashed border-gray-200'
        }`}
      >
        {status === 'success' && (
          <img
            src={displayUrl}
            alt={alt}
            className="h-full w-full object-cover transition-all duration-200"
          />
        )}

        {status === 'loading' && (
          <div className="flex flex-col items-center justify-center gap-2 text-gray-400">
            <Loader2 size={24} className="animate-spin text-primary-500" />
            <p className="text-xs">Đang kiểm tra và tải ảnh...</p>
          </div>
        )}

        {status === 'error' && (
          <div className="flex flex-col items-center justify-center gap-1 p-3 text-center text-red-500">
            <ImageOff size={28} className="text-red-400" />
            <p className="text-xs font-semibold">File ảnh bị lỗi hoặc không thể hiển thị</p>
            <p className="text-[11px] text-gray-500">Vui lòng chọn file ảnh khác</p>
          </div>
        )}

        {status === 'invalid' && (
          <div className="flex flex-col items-center justify-center gap-1 p-3 text-center text-amber-600">
            <AlertCircle size={26} className="text-amber-500" />
            <p className="text-xs font-semibold">Định dạng ảnh chưa hợp lệ</p>
            <p className="text-[11px] text-gray-500">Vui lòng chọn file hình ảnh hợp lệ (PNG, JPG, WEBP, GIF)</p>
          </div>
        )}

        {status === 'idle' && (
          <div className="flex flex-col items-center justify-center gap-1.5 p-4 text-center text-gray-400">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gray-100 text-gray-400">
              <ImageIcon size={22} />
            </div>
            <p className="text-xs font-medium text-gray-500">Chưa có ảnh</p>
            <p className="text-[11px] text-gray-400 max-w-[260px]">
              Bấm chọn file từ máy tính hoặc kéo thả ảnh để xem trước tại đây
            </p>
          </div>
        )}
      </div>
    </div>
  )
}

export default ImagePreview
