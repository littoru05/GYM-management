import { useEffect } from 'react'
import { CheckCircle2, XCircle } from 'lucide-react'

const AUTO_DISMISS_MS = 4000

function CheckInResultBanner({ result, onDismiss }) {
  useEffect(() => {
    if (!result) return
    const timer = setTimeout(() => onDismiss(), AUTO_DISMISS_MS)
    return () => clearTimeout(timer)
  }, [result, onDismiss])

  if (!result) return null

  const isSuccess = result.status === 'SUCCESS'

  return (
    <div
      className={`fixed inset-0 z-50 flex items-center justify-center p-4 ${
        isSuccess ? 'bg-green-900/40' : 'bg-red-900/40'
      }`}
      role="alert"
    >
      <div
        className={`w-full max-w-xl rounded-2xl border-4 bg-white p-8 text-center shadow-2xl ${
          isSuccess ? 'border-green-500' : 'border-red-500'
        }`}
      >
        {isSuccess ? (
          <CheckCircle2 size={72} className="mx-auto text-green-500" />
        ) : (
          <XCircle size={72} className="mx-auto text-red-500" />
        )}

        {isSuccess ? (
          <div className="mt-4">
            <p className="text-2xl font-bold text-green-700">Chào mừng {result.memberName}!</p>
            <div className="mt-4 space-y-1 text-lg text-gray-700">
              <p>
                Mã check-in: <span className="font-semibold">{result.code}</span>
              </p>
              <p>
                Gói tập: <span className="font-semibold">{result.membershipName}</span>
              </p>
              {result.reason && <p className="text-base text-gray-500">{result.reason}</p>}
            </div>
          </div>
        ) : (
          <div className="mt-4">
            <p className="text-2xl font-bold text-red-700">Điểm danh không hợp lệ</p>
            <p className="mt-3 text-lg text-gray-700">{result.reason}</p>
          </div>
        )}

        <button
          type="button"
          onClick={onDismiss}
          className="mt-6 text-sm font-medium text-gray-400 hover:text-gray-600"
        >
          Đóng ngay
        </button>
      </div>
    </div>
  )
}

export default CheckInResultBanner
