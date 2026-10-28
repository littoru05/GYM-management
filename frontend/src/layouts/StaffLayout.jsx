import { Outlet } from 'react-router-dom'
import { ShoppingCart, Users, Tags, ScanLine } from 'lucide-react'
import Header from '../components/common/Header'
import Sidebar from '../components/common/Sidebar'

const NAV_ITEMS = [
  { to: '/staff/check-in', label: 'Điểm danh', icon: ScanLine },
  { to: '/staff/pos', label: 'Bán hàng', icon: ShoppingCart },
  { to: '/staff/members', label: 'Hội viên', icon: Users },
  { to: '/staff/memberships', label: 'Gói tập', icon: Tags },
]

function StaffLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <Header title="PowerFit Gym · Quầy" role="STAFF" />

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
