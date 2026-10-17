import { useState } from 'react'
import { Tag, CheckCircle2, XCircle } from 'lucide-react'
import Input from './Input'
import Button from './Button'

/**
 * Voucher code input + inline validation. Calls `onApply(code)` which should
 * resolve/reject via the promotion service and pass the result back through
 * `result` ({ valid, promotion, reason }).
 */
function VoucherSelector({ onApply, result, applied, onRemove, loading }) {
  const [code, setCode] = useState('')

  const handleApply = () => {
    if (!code.trim()) return
    onApply(code.trim().toUpperCase())
  }

  if (applied) {
    return (
      <div className="flex items-center justify-between rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2.5">
        <div className="flex items-center gap-2 text-sm text-emerald-700">
          <CheckCircle2 size={16} />
          Đã áp dụng mã <span className="font-semibold">{applied.code}</span>
        </div>
        <button
          type="button"
          onClick={onRemove}
          className="text-xs font-medium text-emerald-700 underline hover:text-emerald-800"
        >
          Gỡ bỏ
        </button>
      </div>
    )
  }

  return (
    <div>
      <div className="flex gap-2">
        <Input
          icon={Tag}
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="Nhập mã voucher"
          containerClassName="flex-1"
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              handleApply()
            }
          }}
        />
        <Button variant="secondary" onClick={handleApply} loading={loading}>
          Áp dụng
        </Button>
      </div>
      {result && !result.valid && (
        <p className="mt-1.5 flex items-center gap-1 text-xs text-red-500">
          <XCircle size={13} /> {result.reason}
        </p>
      )}
    </div>
  )
}

export default VoucherSelector
