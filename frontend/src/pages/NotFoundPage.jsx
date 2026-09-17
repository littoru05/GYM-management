import { Link } from 'react-router-dom'
import { Home } from 'lucide-react'

function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4 text-center">
      <p className="text-7xl font-extrabold text-primary-500">404</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Không tìm thấy trang</h1>
      <p className="mt-2 text-gray-500">Trang bạn tìm kiếm không tồn tại hoặc đã bị di chuyển.</p>
      <Link
        to="/"
        className="mt-6 inline-flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-primary-600"
      >
        <Home size={16} /> Về trang chủ
      </Link>
    </div>
  )
}

export default NotFoundPage
