import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout'
import PrivateRoute from './components/routing/PrivateRoute'
import AdminLayout from './layouts/AdminLayout'
import StaffLayout from './layouts/StaffLayout'
import Loading from './components/common/Loading'

// Public Pages (Route-level Code Splitting)
const HomePage = lazy(() => import('./pages/public/HomePage'))
const AboutPage = lazy(() => import('./pages/public/AboutPage'))
const ServicesPage = lazy(() => import('./pages/public/ServicesPage'))
const PricingPage = lazy(() => import('./pages/public/PricingPage'))
const ContactPage = lazy(() => import('./pages/public/ContactPage'))
const LoginPage = lazy(() => import('./pages/auth/LoginPage'))

// Admin Pages (Lazy-loaded to keep admin & chart bundle separate from public bundle)
const DashboardPage = lazy(() => import('./pages/admin/DashboardPage'))
const ReportsPage = lazy(() => import('./pages/admin/ReportsPage'))
const InventoryPage = lazy(() => import('./pages/admin/InventoryPage'))
const StaffManagementPage = lazy(() => import('./pages/admin/StaffManagementPage'))
const VouchersPage = lazy(() => import('./pages/admin/VouchersPage'))

// Staff Pages (Lazy-loaded on demand)
const PosPage = lazy(() => import('./pages/staff/PosPage'))
const CheckInPage = lazy(() => import('./pages/checkin/CheckInPage'))

// Shared Pages
const MembersPage = lazy(() => import('./pages/members/MembersPage'))
const PackagesPage = lazy(() => import('./pages/packages/PackagesPage'))
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'))

function PageFallback() {
  return (
    <div className="min-h-[50vh] flex items-center justify-center p-8">
      <Loading label="Đang tải trang..." size={32} />
    </div>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageFallback />}>
        <Routes>
          <Route element={<PublicLayout />}>
            <Route index element={<HomePage />} />
            <Route path="about" element={<AboutPage />} />
            <Route path="services" element={<ServicesPage />} />
            <Route path="pricing" element={<PricingPage />} />
            <Route path="contact" element={<ContactPage />} />
          </Route>

          <Route path="/login" element={<LoginPage />} />

          <Route element={<PrivateRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="reports" element={<ReportsPage />} />
              <Route path="members" element={<MembersPage />} />
              <Route path="staffs" element={<StaffManagementPage />} />
              <Route path="staff" element={<StaffManagementPage />} />
              <Route path="memberships" element={<PackagesPage />} />
              <Route path="inventory" element={<InventoryPage />} />
              <Route path="vouchers" element={<VouchersPage />} />
              <Route path="promotions" element={<VouchersPage />} />
            </Route>
          </Route>

          <Route element={<PrivateRoute allowedRoles={['STAFF']} />}>
            <Route path="/staff" element={<StaffLayout />}>
              <Route path="pos" element={<PosPage />} />
              <Route path="check-in" element={<CheckInPage />} />
              <Route path="members" element={<MembersPage />} />
              <Route path="memberships" element={<PackagesPage />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}

export default App

