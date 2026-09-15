import { Outlet } from 'react-router-dom'
import PublicNavbar from '../components/public/PublicNavbar'
import PublicFooter from '../components/public/PublicFooter'
import FloatingContactButtons from '../components/public/FloatingContactButtons'

function PublicLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-white">
      <PublicNavbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <PublicFooter />
      <FloatingContactButtons />
    </div>
  )
}

export default PublicLayout
