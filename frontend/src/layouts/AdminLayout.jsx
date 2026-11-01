import { Outlet } from 'react-router-dom'
import { LayoutDashboard, Users, UserCog, Tags, Warehouse, TicketPercent, BarChart3 } from 'lucide-react'
import Header from '../components/common/Header'
import Sidebar from '../components/common/Sidebar'

const NAV_ITEMS = [
  { to: '/admin/dashboard', label: 'Bảng điều khiển', icon: LayoutDashboard },
  { to: '/admin/reports', label: 'Báo cáo doanh thu', icon: BarChart3 },
  { to: '/admin/members', label: 'Hội viên', icon: Users },
  { to: '/admin/staffs', label: 'Nhân viên', icon: UserCog },
  { to: '/admin/memberships', label: 'Gói tập', icon: Tags },
  { to: '/admin/inventory', label: 'Kho hàng', icon: Warehouse },
  { to: '/admin/vouchers', label: 'Voucher', icon: TicketPercent },
]

function AdminLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <Header title="PowerFit Gym · Admin" role="ADMIN" />

      <div className="flex flex-1">
        <Sidebar items={NAV_ITEMS} />
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AdminLayout
