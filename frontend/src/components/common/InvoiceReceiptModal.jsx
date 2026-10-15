import { useEffect } from 'react'
import { createPortal } from 'react-dom'
import { Printer, PlusCircle, Dumbbell, X, CheckCircle2 } from 'lucide-react'
import Button from './Button'
import { formatCurrency, formatDateTime } from '../../utils/formatters'
import { PAYMENT_METHOD_LABEL } from '../../utils/constants'

/**
 * InvoiceReceiptModal component:
 * Modal hiển thị phiếu thu bán lẻ chuẩn chuyên nghiệp cho PowerFit Gym.
 * - Sử dụng createPortal để overlay bao phủ toàn bộ 100% màn hình (kể cả Header & Sidebar).
 * - Tích hợp in hóa đơn cách ly qua iframe bảo đảm in nhiệt hoặc in laser chuẩn 100%.
 * - Bố cục cân đối, có thanh tiêu đề rõ ràng, loại bỏ khoảng trắng thừa trên đầu.
 */
function InvoiceReceiptModal({ open, onClose, invoice, onNewOrder }) {
  useEffect(() => {
    if (!open) return
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') onClose?.()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [open, onClose])

  if (!open || !invoice) return null

  const items = invoice.items || []
  const subtotal = invoice.subtotal || invoice.total + (invoice.discountAmount || invoice.discount || 0)
  const discount = Number(invoice.discountAmount || invoice.discount || 0)
  const total = Number(invoice.total || 0)
  const paymentMethodLabel = PAYMENT_METHOD_LABEL[invoice.paymentMethod] || invoice.paymentMethod || 'Tiền mặt'

  const handlePrint = () => {
    // Tạo iframe ẩn cách ly để in riêng phiếu thu sắc nét, không bị dính CSS layout trang web
    const iframe = document.createElement('iframe')
    iframe.style.position = 'fixed'
    iframe.style.right = '0'
    iframe.style.bottom = '0'
    iframe.style.width = '0'
    iframe.style.height = '0'
    iframe.style.border = '0'
    document.body.appendChild(iframe)

    const doc = iframe.contentWindow.document
    doc.open()
    doc.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8" />
          <title>Hóa đơn bán lẻ - ${invoice.id || 'PowerFit Gym'}</title>
          <style>
            @page {
              size: 80mm auto;
              margin: 4mm;
            }
            * {
              box-sizing: border-box;
              margin: 0;
              padding: 0;
              -webkit-print-color-adjust: exact !important;
              print-color-adjust: exact !important;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
              color: #111827;
              background: #ffffff;
              width: 100%;
              max-width: 300px;
              margin: 0 auto;
              padding: 6px;
              font-size: 12px;
              line-height: 1.4;
            }
            .header {
              text-align: center;
              margin-bottom: 8px;
            }
            .brand-name {
              font-size: 16px;
              font-weight: 900;
              letter-spacing: 0.5px;
            }
            .brand-sub {
              font-size: 10px;
              font-weight: 700;
              color: #ea580c;
            }
            .brand-info {
              font-size: 10px;
              color: #4b5563;
              margin-top: 2px;
            }
            .divider {
              border-top: 1px dashed #9ca3af;
              margin: 8px 0;
            }
            .title-box {
              text-align: center;
              padding: 4px 0;
            }
            .title {
              font-size: 13px;
              font-weight: 800;
              letter-spacing: 1px;
            }
            .meta-row {
              display: flex;
              justify-content: space-between;
              font-size: 11px;
              color: #374151;
              margin-bottom: 2px;
            }
            .meta-label { color: #6b7280; }
            .meta-val { font-weight: 600; color: #111827; }
            table {
              width: 100%;
              border-collapse: collapse;
              margin: 6px 0;
              font-size: 11px;
            }
            th {
              border-top: 1px dashed #9ca3af;
              border-bottom: 1px dashed #9ca3af;
              padding: 5px 2px;
              font-size: 10px;
              font-weight: 700;
              text-transform: uppercase;
              color: #4b5563;
            }
            td {
              padding: 4px 2px;
              vertical-align: top;
            }
            .item-name { font-weight: 600; color: #111827; }
            .calc-row {
              display: flex;
              justify-content: space-between;
              font-size: 11px;
              margin-bottom: 2px;
            }
            .total-row {
              display: flex;
              justify-content: space-between;
              border-top: 1px solid #111827;
              padding-top: 5px;
              margin-top: 5px;
              font-size: 14px;
              font-weight: 900;
            }
            .barcode {
              display: flex;
              justify-content: center;
              align-items: flex-end;
              gap: 2px;
              height: 28px;
              margin: 10px 0 2px;
            }
            .bar { background: #000; height: 100%; }
            .footer-text {
              text-align: center;
              font-size: 10px;
              color: #4b5563;
              margin-top: 6px;
            }
          </style>
        </head>
        <body>
          <div class="header">
            <div class="brand-name">POWERFIT GYM</div>
            <div class="brand-sub">FITNESS & RECOVERY CENTER</div>
            <div class="brand-info">123 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh</div>
            <div class="brand-info">Hotline: 1900 6868 • Website: powerfitgym.vn</div>
          </div>

          <div class="divider"></div>

          <div class="title-box">
            <div class="title">HÓA ĐƠN BÁN LẺ</div>
            <div style="font-family: monospace; font-size: 11px; font-weight: bold; color: #4b5563;">
              Mã HĐ: ${invoice.id || invoice.transactionId || 'HD-POS'}
            </div>
          </div>

          <div class="divider"></div>

          <div class="meta-row">
            <span class="meta-label">Thời gian:</span>
            <span class="meta-val">${formatDateTime(invoice.date)}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">Thu ngân:</span>
            <span class="meta-val">${invoice.staffName || 'Thu ngân quầy'}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">Khách hàng:</span>
            <span class="meta-val">${invoice.memberName || 'Khách lẻ'}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">Thanh toán:</span>
            <span class="meta-val">${paymentMethodLabel}</span>
          </div>

          <table>
            <thead>
              <tr>
                <th style="text-align: left; width: 45%;">Tên món</th>
                <th style="text-align: center; width: 15%;">SL</th>
                <th style="text-align: right; width: 20%;">Đơn giá</th>
                <th style="text-align: right; width: 20%;">T.Tiền</th>
              </tr>
            </thead>
            <tbody>
              ${items
                .map(
                  (item) => `
                <tr>
                  <td class="item-name">${item.name}</td>
                  <td style="text-align: center; font-weight: bold;">${item.quantity}</td>
                  <td style="text-align: right; color: #4b5563;">${formatCurrency(item.price)}</td>
                  <td style="text-align: right; font-weight: bold;">${formatCurrency(item.price * item.quantity)}</td>
                </tr>
              `
                )
                .join('')}
            </tbody>
          </table>

          <div class="divider"></div>

          <div class="calc-row">
            <span>Tạm tính:</span>
            <span style="font-weight: 600;">${formatCurrency(subtotal)}</span>
          </div>
          ${
            discount > 0
              ? `
            <div class="calc-row" style="color: #059669; font-weight: 600;">
              <span>Giảm giá:</span>
              <span>-${formatCurrency(discount)}</span>
            </div>
          `
              : ''
          }
          <div class="total-row">
            <span>TỔNG CỘNG:</span>
            <span>${formatCurrency(total)}</span>
          </div>

          <div class="barcode">
            ${[2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 2, 3, 1, 4, 2, 1, 3, 1, 2, 4, 1, 3, 2, 1]
              .map((w, i) => `<div class="bar" style="width: ${w}px; height: ${i % 2 === 0 ? '100%' : '85%'};"></div>`)
              .join('')}
          </div>
          <div style="text-align: center; font-family: monospace; font-size: 10px; color: #6b7280; letter-spacing: 1px;">
            *${invoice.id || 'HD-POWERFIT'}*
          </div>

          <div class="footer-text">
            <p style="font-weight: 600;">Cảm ơn quý khách và hẹn gặp lại!</p>
            <p style="font-size: 9px; color: #9ca3af; margin-top: 2px;">Chúc bạn có buổi tập tràn đầy năng lượng & hiệu quả!</p>
          </div>
        </body>
      </html>
    `)
    doc.close()

    setTimeout(() => {
      iframe.contentWindow.focus()
      iframe.contentWindow.print()
      setTimeout(() => {
        if (document.body.contains(iframe)) {
          document.body.removeChild(iframe)
        }
      }, 1500)
    }, 250)
  }

  const handleNewOrder = () => {
    if (onNewOrder) {
      onNewOrder()
    } else {
      onClose?.()
    }
  }

  return createPortal(
    <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60 p-4 backdrop-blur-xs">
      <div
        role="dialog"
        aria-modal="true"
        className="relative flex flex-col w-full max-w-lg max-h-[92vh] rounded-2xl bg-white shadow-2xl animate-fade-in overflow-hidden"
      >
        {/* Modal Header Bar */}
        <div className="flex items-center justify-between border-b border-gray-100 px-5 py-3.5 shrink-0 bg-white">
          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-100 text-emerald-600">
              <CheckCircle2 size={16} />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900 leading-none">Hóa đơn thanh toán</h3>
              <p className="text-[11px] text-gray-400 mt-0.5">Thanh toán hoàn tất</p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="Đóng"
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Scrollable Receipt Body Container */}
        <div className="flex-1 overflow-y-auto px-5 py-4 sm:px-6">
          <div id="pos-invoice-receipt" className="bg-white">
            {/* Brand Header */}
            <div className="flex flex-col items-center text-center">
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary-500 text-white shadow-md">
                <Dumbbell size={22} strokeWidth={2.5} />
              </div>
              <h2 className="mt-2 text-lg font-black tracking-wide text-gray-900">POWERFIT GYM</h2>
              <p className="text-[11px] font-semibold text-primary-600">FITNESS & RECOVERY CENTER</p>
              <p className="mt-0.5 text-[11px] text-gray-500">
                123 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh
              </p>
              <p className="text-[11px] text-gray-500">
                Hotline: 1900 6868 • Website: powerfitgym.vn
              </p>
            </div>

            {/* Receipt Title & Divider */}
            <div className="my-3 border-y border-dashed border-gray-300 py-2 text-center">
              <h3 className="text-sm font-extrabold uppercase tracking-widest text-gray-800">
                HÓA ĐƠN BÁN LẺ
              </h3>
              <p className="mt-0.5 font-mono text-xs font-bold text-gray-500">
                Mã HĐ: {invoice.id || invoice.transactionId || 'HD-POS'}
              </p>
            </div>

            {/* Transaction Metadata */}
            <div className="space-y-1 text-xs text-gray-600">
              <div className="flex justify-between">
                <span className="text-gray-500">Thời gian:</span>
                <span className="font-medium text-gray-800">{formatDateTime(invoice.date)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Thu ngân:</span>
                <span className="font-medium text-gray-800">{invoice.staffName || 'Thu ngân quầy'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Khách hàng:</span>
                <span className="font-medium text-gray-800">{invoice.memberName || 'Khách lẻ'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Hình thức thanh toán:</span>
                <span className="font-semibold text-gray-800">{paymentMethodLabel}</span>
              </div>
            </div>

            {/* Purchased Items Table */}
            <div className="mt-3.5">
              <div className="grid grid-cols-12 border-y border-dashed border-gray-300 bg-gray-50/70 py-1.5 text-[11px] font-bold text-gray-600 uppercase">
                <span className="col-span-6 pl-1">Tên món</span>
                <span className="col-span-2 text-center">SL</span>
                <span className="col-span-2 text-right">Đơn giá</span>
                <span className="col-span-2 text-right pr-1">T.Tiền</span>
              </div>

              <div className="divide-y divide-gray-100 py-1">
                {items.map((item, idx) => (
                  <div key={idx} className="grid grid-cols-12 py-1.5 text-xs text-gray-700 items-center">
                    <div className="col-span-6 pl-1 pr-2">
                      <p className="font-semibold text-gray-900 leading-tight">{item.name}</p>
                    </div>
                    <div className="col-span-2 text-center font-bold text-gray-800">
                      {item.quantity}
                    </div>
                    <div className="col-span-2 text-right text-[11px] text-gray-500">
                      {formatCurrency(item.price)}
                    </div>
                    <div className="col-span-2 text-right pr-1 font-bold text-gray-900">
                      {formatCurrency(item.price * item.quantity)}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Payment Calculation Summary */}
            <div className="mt-2 border-t border-dashed border-gray-300 pt-2 space-y-1.5 text-xs">
              <div className="flex justify-between text-gray-600">
                <span>Tạm tính:</span>
                <span className="font-medium text-gray-900">{formatCurrency(subtotal)}</span>
              </div>

              {discount > 0 && (
                <div className="flex justify-between font-medium text-emerald-600">
                  <span>Giảm giá:</span>
                  <span>-{formatCurrency(discount)}</span>
                </div>
              )}

              <div className="flex items-center justify-between border-t border-gray-200 pt-2 text-sm">
                <span className="font-extrabold text-gray-900 uppercase">Tổng tiền thanh toán:</span>
                <span className="text-lg font-black text-primary-600 tracking-tight">
                  {formatCurrency(total)}
                </span>
              </div>
            </div>

            {/* Barcode Simulation */}
            <div className="mt-3.5 flex flex-col items-center border-t border-dashed border-gray-200 pt-2.5">
              <div className="flex h-7 items-end gap-[2px] opacity-80" aria-hidden="true">
                {[2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 2, 3, 1, 4, 2, 1, 3, 1, 2, 4, 1, 3, 2, 1].map((w, i) => (
                  <div
                    key={i}
                    className="bg-black"
                    style={{ width: `${w}px`, height: i % 2 === 0 ? '100%' : '85%' }}
                  />
                ))}
              </div>
              <p className="mt-1 font-mono text-[10px] tracking-widest text-gray-500 uppercase">
                *{invoice.id || 'HD-POWERFIT'}*
              </p>
            </div>

            {/* Thank You Footer */}
            <div className="mt-2.5 text-center text-xs text-gray-500">
              <p className="font-medium text-gray-700">Cảm ơn quý khách và hẹn gặp lại!</p>
              <p className="text-[11px] text-gray-400 mt-0.5">
                Chúc bạn có buổi tập tràn đầy năng lượng & hiệu quả!
              </p>
            </div>
          </div>
        </div>

        {/* Modal Action Buttons */}
        <div className="flex items-center justify-end gap-2.5 border-t border-gray-100 bg-gray-50/80 px-5 py-3 rounded-b-2xl shrink-0">
          <Button
            type="button"
            variant="secondary"
            size="md"
            icon={Printer}
            onClick={handlePrint}
            className="border-gray-200 shadow-xs hover:border-gray-300"
          >
            In hóa đơn
          </Button>

          <Button
            type="button"
            variant="primary"
            size="md"
            icon={PlusCircle}
            onClick={handleNewOrder}
            className="shadow-sm"
          >
            Tạo đơn mới
          </Button>
        </div>
      </div>
    </div>,
    document.body
  )
}

export default InvoiceReceiptModal
