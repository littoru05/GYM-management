import { useEffect, useRef, useState } from 'react'
import toast from 'react-hot-toast'
import { ScanLine, UserCheck } from 'lucide-react'
import Badge from '../../components/common/Badge'
import Loading from '../../components/common/Loading'
import EmptyState from '../../components/common/EmptyState'
import { useCheckInMutation, useTodayCheckInsQuery } from '../../hooks/useCheckIns'
import CheckInResultBanner from './CheckInResultBanner'

const STATUS_BADGE = {
  SUCCESS: { color: 'green', label: 'Hợp lệ' },
  DENIED: { color: 'red', label: 'Từ chối' },
}

function formatTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const PLACEHOLDER_TEXT = 'Ví dụ: 0987654321 hoặc MEM-0001'

function CheckInPage() {
  const [inputValue, setInputValue] = useState('')
  const [result, setResult] = useState(null)
  const [isInputFocused, setIsInputFocused] = useState(false)
  const inputRef = useRef(null)

  const { data: todayCheckIns, isLoading } = useTodayCheckInsQuery()
  const checkInMutation = useCheckInMutation()

  const focusInput = () => {
    requestAnimationFrame(() => inputRef.current?.focus())
  }

  useEffect(() => {
    focusInput()
  }, [])

  const handleDismissResult = () => {
    setResult(null)
    focusInput()
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const identifier = inputValue.trim()

    if (!identifier) {
      toast.error('Vui lòng nhập số điện thoại hoặc mã hội viên.')
      return
    }
    if (identifier.length < 3) {
      toast.error('Số điện thoại hoặc mã hội viên không đúng định dạng.')
      return
    }
    if (checkInMutation.isPending) return

    checkInMutation.mutate(identifier, {
      onSuccess: (data) => {
        setResult(data)
        setInputValue('')
      },
      onError: (error) => {
        if (!error.response) {
          toast.error('Mất kết nối tới máy chủ. Vui lòng kiểm tra lại đường truyền.')
          return
        }
        setResult({
          status: 'DENIED',
          reason: error.response?.data?.message || 'Không thể điểm danh. Vui lòng thử lại.',
        })
        setInputValue('')
      },
      onSettled: () => {
        focusInput()
      },
    })
  }

  const successCount = (todayCheckIns || []).filter((c) => c.status === 'SUCCESS').length

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Điểm danh Check-in</h1>
          <p className="mt-1 text-gray-500">Nhập số điện thoại hoặc mã hội viên để điểm danh tại quầy.</p>
        </div>
        <div className="rounded-xl border border-gray-100 bg-white px-5 py-3 text-center shadow-sm">
          <p className="text-xs font-medium uppercase text-gray-400">Đã check-in hôm nay</p>
          <p className="text-3xl font-bold text-primary-500">{successCount}</p>
        </div>
      </div>

      <form
        onSubmit={handleSubmit}
        className="mx-auto mt-8 max-w-2xl rounded-2xl border border-gray-100 bg-white p-8 shadow-sm"
      >
        <label className="mb-3 block text-center text-sm font-medium text-gray-500">
          Số điện thoại hoặc mã hội viên
        </label>
        <div className="relative">
          <ScanLine
            size={28}
            className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-300"
          />
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            disabled={checkInMutation.isPending}
            autoFocus
            onFocus={() => setIsInputFocused(true)}
            onBlur={() => setIsInputFocused(false)}
            placeholder={isInputFocused ? '' : PLACEHOLDER_TEXT}
            className="w-full rounded-xl border-2 border-gray-200 bg-white py-5 pl-14 pr-4 text-center text-2xl font-semibold text-gray-900 tracking-wide focus:border-primary-400 focus:outline-none focus:ring-4 focus:ring-primary-100 disabled:bg-gray-50 disabled:text-gray-400"
          />
        </div>
        <button
          type="submit"
          disabled={checkInMutation.isPending}
          className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl bg-primary-500 py-3 text-lg font-semibold text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <UserCheck size={20} />
          {checkInMutation.isPending ? 'Đang xử lý...' : 'Điểm danh (Enter)'}
        </button>
      </form>

      <div className="mt-8 rounded-xl border border-gray-100 bg-white shadow-sm">
        <div className="border-b border-gray-100 px-5 py-4">
          <h2 className="font-semibold text-gray-900">Lịch sử check-in hôm nay</h2>
        </div>

        {isLoading ? (
          <div className="p-5">
            <Loading label="Đang tải lịch sử..." />
          </div>
        ) : !todayCheckIns || todayCheckIns.length === 0 ? (
          <EmptyState title="Chưa có lượt check-in nào" description="Danh sách sẽ hiển thị ngay khi có hội viên điểm danh." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-gray-100 text-xs uppercase text-gray-400">
                <tr>
                  <th className="px-4 py-3">Thời gian</th>
                  <th className="px-4 py-3">Mã check-in</th>
                  <th className="px-4 py-3">Họ tên</th>
                  <th className="px-4 py-3">Số điện thoại</th>
                  <th className="px-4 py-3">Gói tập</th>
                  <th className="px-4 py-3">Trạng thái</th>
                  <th className="px-4 py-3">Nhân viên</th>
                </tr>
              </thead>
              <tbody>
                {todayCheckIns.map((item) => {
                  const badge = STATUS_BADGE[item.status] || { color: 'gray', label: item.status }
                  return (
                    <tr key={item.id || item.code} className="border-b border-gray-50">
                      <td className="px-4 py-3 text-gray-600">{formatTime(item.time)}</td>
                      <td className="px-4 py-3 font-medium text-gray-900">{item.code || '—'}</td>
                      <td className="px-4 py-3 text-gray-700">{item.memberName || '—'}</td>
                      <td className="px-4 py-3 text-gray-600">{item.phone || '—'}</td>
                      <td className="px-4 py-3 text-gray-600">{item.membershipName || '—'}</td>
                      <td className="px-4 py-3">
                        <Badge color={badge.color}>{badge.label}</Badge>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{item.staffName || '—'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <CheckInResultBanner result={result} onDismiss={handleDismissResult} />
    </div>
  )
}

export default CheckInPage
