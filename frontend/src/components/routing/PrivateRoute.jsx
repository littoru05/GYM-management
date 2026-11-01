import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../../store/useAuthStore'

const ROLE_HOME = {
  ADMIN: '/admin/dashboard',
  STAFF: '/staff/pos',
}

function PrivateRoute({ allowedRoles }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const user = useAuthStore((state) => state.user)

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to={ROLE_HOME[user?.role] || '/login'} replace />
  }

  return <Outlet />
}

export default PrivateRoute
