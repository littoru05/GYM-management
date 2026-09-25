import { Outlet } from 'react-router-dom'
import { Dumbbell, LogOut, ShoppingCart, Users, Tags, ScanLine } from 'lucide-react'
import { useAuthStore } from '../store/useAuthStore'
import { useLogout } from '../hooks/useAuth'
import Sidebar from '../components/common/Sidebar'

const NAV_ITEMS = [
  { to: '/staff/check-in', label: 'Điểm danh', icon: ScanLine },
  { to: '/staff/pos', label: 'Bán hàng', icon: ShoppingCart },
  { to: '/staff/members', label: 'Hội viên', icon: Users },
  { to: '/staff/memberships', label: 'Gói tập', icon: Tags },
]

function StaffLayout() {
  const user = useAuthStore((state) => state.user)
  const handleLogout = useLogout()

  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <header className="flex h-16 items-center justify-between border-b border-gray-100 bg-white px-6">
        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-500 text-white">
            <Dumbbell size={18} />
          </div>
          <span className="text-lg font-bold text-gray-900">PowerFit Gym · Quầy</span>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-sm font-medium text-gray-600">{user?.name}</span>
          <button
            type="button"
            onClick={handleLogout}
            className="flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100"
          >
            <LogOut size={16} />
            Đăng xuất
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        <Sidebar items={NAV_ITEMS} />
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default StaffLayout
