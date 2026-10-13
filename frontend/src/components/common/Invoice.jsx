import { CheckCircle2, Dumbbell } from 'lucide-react'
import { formatCurrency, formatDateTime } from '../../utils/formatters'
import { PAYMENT_METHOD_LABEL } from '../../utils/constants'

/**
 * Printable-looking receipt shown after a successful POS checkout.
 */
function Invoice({ invoice }) {
  if (!invoice) return null

  return (
    <div className="mx-auto max-w-sm">
      <div className="flex flex-col items-center text-center">
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-600">
          <CheckCircle2 size={26} />
        </div>
        <p className="mt-3 text-lg font-bold text-gray-900">Thanh toán thành công</p>
        <p className="text-xs text-gray-400">{formatDateTime(invoice.date)}</p>
      </div>

      <div className="mt-5 rounded-2xl border border-dashed border-gray-200 bg-gray-50/50 p-4">
        <div className="flex items-center justify-between text-xs text-gray-500">
          <span className="flex items-center gap-1 font-semibold text-gray-700">
            <Dumbbell size={14} className="text-primary-500" /> PowerFit Gym · POS
          </span>
          <span className="font-mono text-xs">{invoice.id}</span>
        </div>

        <div className="mt-3 space-y-2 border-t border-gray-200/80 pt-3">
          {invoice.items.map((item, idx) => (
            <div key={idx} className="flex justify-between text-sm">
              <span className="text-gray-700">
                {item.name} {item.quantity > 1 ? `x${item.quantity}` : ''}
              </span>
              <span className="font-semibold text-gray-900">
                {formatCurrency(item.price * item.quantity)}
              </span>
            </div>
          ))}
        </div>

        {invoice.discountAmount > 0 && (
          <div className="mt-2 flex justify-between border-t border-gray-200/80 pt-2 text-sm text-emerald-600">
            <span>Giảm giá</span>
            <span>-{formatCurrency(invoice.discountAmount)}</span>
          </div>
        )}

        <div className="mt-3 flex justify-between border-t border-gray-200/80 pt-3 text-base font-bold text-gray-900">
          <span>Tổng tiền</span>
          <span className="text-primary-600">{formatCurrency(invoice.total)}</span>
        </div>

        <div className="mt-2 flex items-center justify-between text-xs text-gray-500">
          <span>Hình thức thanh toán:</span>
          <span className="font-medium text-gray-700">
            {PAYMENT_METHOD_LABEL[invoice.paymentMethod] || invoice.paymentMethod}
          </span>
        </div>
      </div>
    </div>
  )
}

export default Invoice
