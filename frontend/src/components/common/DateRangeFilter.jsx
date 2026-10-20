import { useState } from 'react'
import { Calendar, Filter, Sparkles, AlertCircle } from 'lucide-react'
import Button from './Button.jsx'

const RANGE_OPTIONS = [
  { value: '7d', label: '7 ngày' },
  { value: '30d', label: '30 ngày' },
  { value: '12m', label: '12 tháng' },
  { value: 'custom', label: 'Tùy chọn ngày' },
]

function DateRangeFilter({
  range,
  onRangeChange,
  customStartDate,
  customEndDate,
  onCustomDateChange,
  simulateEmpty = false,
  onToggleSimulateEmpty,
  className = '',
}) {
  const [localStart, setLocalStart] = useState(() => {
    return customStartDate || new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().slice(0, 10)
  })
  const [localEnd, setLocalEnd] = useState(() => {
    return customEndDate || new Date().toISOString().slice(0, 10)
  })
  const [dateError, setDateError] = useState('')


  const validateDates = (start, end) => {
    if (start && end && new Date(start) > new Date(end)) {
      setDateError('Ngày bắt đầu không được lớn hơn ngày kết thúc')
      return false
    }
    setDateError('')
    return true
  }

  const handleApplyCustom = (e) => {
    e.preventDefault()
    if (!localStart || !localEnd) {
      setDateError('Vui lòng chọn đầy đủ từ ngày và đến ngày')
      return
    }
    if (!validateDates(localStart, localEnd)) {
      return
    }
    onCustomDateChange(localStart, localEnd)
  }

  return (
    <div className={`space-y-3 ${className}`}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        {/* Quick range tabs */}
        <div className="flex flex-wrap items-center gap-1.5 rounded-xl border border-gray-200/80 bg-white p-1 shadow-sm">
          {RANGE_OPTIONS.map((opt) => {
            const isActive = range === opt.value
            return (
              <button
                key={opt.value}
                type="button"
                onClick={() => {
                  setDateError('')
                  onRangeChange(opt.value)
                }}
                className={`flex items-center gap-1.5 rounded-lg px-3.5 py-1.5 text-xs font-semibold transition-all duration-150 ${
                  isActive
                    ? 'bg-primary-500 text-white shadow-sm'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`}
              >
                {opt.value === 'custom' && <Calendar size={13} />}
                {opt.label}
              </button>
            )
          })}
        </div>

        {/* DoD Test helper: Toggle simulate empty */}
        {onToggleSimulateEmpty && (
          <button
            type="button"
            onClick={onToggleSimulateEmpty}
            className={`inline-flex items-center gap-1.5 rounded-lg border px-3 py-1.5 text-xs font-medium transition-colors ${
              simulateEmpty
                ? 'border-amber-300 bg-amber-50 text-amber-700 hover:bg-amber-100'
                : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300 hover:text-gray-700'
            }`}
            title="Thử nghiệm trạng thái rỗng (Empty State) theo DoD"
          >
            <Sparkles size={13} className={simulateEmpty ? 'text-amber-500' : 'text-gray-400'} />
            {simulateEmpty ? 'Đang bật Empty State (Click để tắt)' : 'Mô phỏng dữ liệu rỗng'}
          </button>
        )}
      </div>

      {/* Expanded Custom Date Picker */}
      {range === 'custom' && (
        <form
          onSubmit={handleApplyCustom}
          className="flex flex-wrap items-end gap-3 rounded-xl border border-gray-100 bg-gray-50/80 p-3.5"
        >
          <div>
            <label className="mb-1 block text-xs font-medium text-gray-600">Từ ngày</label>
            <input
              type="date"
              value={localStart}
              onChange={(e) => {
                const newStart = e.target.value
                setLocalStart(newStart)
                validateDates(newStart, localEnd)
              }}
              className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-medium text-gray-600">Đến ngày</label>
            <input
              type="date"
              value={localEnd}
              onChange={(e) => {
                const newEnd = e.target.value
                setLocalEnd(newEnd)
                validateDates(localStart, newEnd)
              }}
              className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
            />
          </div>

          <Button type="submit" size="sm" icon={Filter}>
            Áp dụng
          </Button>

          {dateError && (
            <p className="flex w-full items-center gap-1 text-xs text-red-500">
              <AlertCircle size={13} />
              {dateError}
            </p>
          )}
        </form>
      )}
    </div>
  )
}

export default DateRangeFilter
