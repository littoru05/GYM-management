import React from 'react'
import { AlertTriangle, RefreshCw, Home, WifiOff } from 'lucide-react'

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    console.error('Lỗi giao diện bị chặn bởi ErrorBoundary:', error, errorInfo)
  }

  componentDidMount() {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', this.handleOnline)
    }
  }

  componentWillUnmount() {
    if (typeof window !== 'undefined') {
      window.removeEventListener('online', this.handleOnline)
    }
  }

  handleOnline = () => {
    if (this.state.hasError) {
      this.setState({ hasError: false, error: null })
    }
  }

  handleRetryOrReload = () => {
    this.setState({ hasError: false, error: null })
    setTimeout(() => {
      if (this.state.hasError && typeof window !== 'undefined') {
        window.location.reload()
      }
    }, 150)
  }

  handleGoHome = () => {
    if (typeof window !== 'undefined') {
      window.location.href = '/'
    }
  }

  render() {
    if (this.state.hasError) {
      const errStr = (this.state.error?.message || '').toLowerCase()
      const errCode = (this.state.error?.code || '').toUpperCase()

      const isTimeout =
        errCode === 'ECONNABORTED' ||
        errCode === 'ETIMEDOUT' ||
        errStr.includes('timeout') ||
        errStr.includes('econnaborted')

      const isNetworkIssue =
        (typeof window !== 'undefined' && !window.navigator.onLine) ||
        isTimeout ||
        errCode === 'ERR_NETWORK' ||
        errCode === 'ECONNRESET' ||
        errStr.includes('network') ||
        errStr.includes('fetch') ||
        errStr.includes('alb')

      return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4 py-12">
          <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-card border border-gray-100">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-amber-50 text-amber-600 mb-6 ring-8 ring-amber-50/50">
              {isNetworkIssue ? <WifiOff size={32} /> : <AlertTriangle size={32} />}
            </div>

            <h2 className="text-xl font-bold text-gray-900">
              {isTimeout
                ? 'Hết thời gian chờ phản hồi (Timeout)'
                : isNetworkIssue
                ? 'Mất kết nối máy chủ (ALB)'
                : 'Đã xảy ra sự cố hiển thị'}
            </h2>

            <p className="mt-3 text-sm leading-relaxed text-gray-600">
              {isTimeout
                ? 'Đường truyền mạng từ máy khách tới máy chủ (ALB) phản hồi quá chậm hoặc quá thời gian chờ (ECONNABORTED). Dữ liệu của bạn vẫn an toàn.'
                : isNetworkIssue
                ? 'Không thể kết nối đến máy chủ hoặc đường truyền internet tới ALB bị ngắt quãng. Giao diện được bảo vệ an toàn chống lỗi màn hình trắng.'
                : 'Hệ thống vừa gặp một lỗi bất ngờ khi hiển thị giao diện. Dữ liệu của bạn vẫn an toàn.'}
            </p>

            <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <button
                type="button"
                onClick={this.handleRetryOrReload}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-primary-500 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:bg-primary-600 active:scale-[0.98]"
              >
                <RefreshCw size={16} />
                Thử lại kết nối
              </button>
              <button
                type="button"
                onClick={this.handleGoHome}
                className="inline-flex items-center justify-center gap-2 rounded-xl border border-gray-300 bg-white px-5 py-2.5 text-sm font-semibold text-gray-700 transition-all hover:bg-gray-50 active:scale-[0.98]"
              >
                <Home size={16} />
                Về trang chủ
              </button>
            </div>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary
