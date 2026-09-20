import { useState } from 'react'
import { NavLink, Link } from 'react-router-dom'
import { Dumbbell, Menu, X, Phone, LogIn } from 'lucide-react'

const NAV_LINKS = [
  { to: '/', label: 'Trang chủ' },
  { to: '/about', label: 'Giới thiệu' },
  { to: '/services', label: 'Dịch vụ' },
  { to: '/pricing', label: 'Bảng giá' },
  { to: '/contact', label: 'Liên hệ' },
]

function PublicNavbar() {
  const [open, setOpen] = useState(false)

  return (
    <header className="sticky top-0 z-40 border-b border-gray-100 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <Link to="/" className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-500 text-white">
            <Dumbbell size={18} />
          </div>
          <span className="text-lg font-bold text-gray-900">PowerFit Gym</span>
        </Link>

        <nav className="hidden items-center gap-8 lg:flex">
          {NAV_LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `text-sm font-medium transition-colors ${isActive ? 'text-primary-600' : 'text-gray-600 hover:text-primary-600'}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          <a href="tel:19001234" className="flex items-center gap-1.5 text-sm font-medium text-gray-600">
            <Phone size={16} />
            1900 1234
          </a>
          <Link
            to="/login"
            aria-label="Đăng nhập"
            title="Đăng nhập"
            className="flex h-9 w-9 items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
          >
            <LogIn size={18} />
          </Link>
        </div>

        <button
          type="button"
          className="rounded-lg p-2 text-gray-600 hover:bg-gray-100 lg:hidden"
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {open && (
        <div className="border-t border-gray-100 bg-white px-4 py-3 lg:hidden">
          <nav className="flex flex-col gap-1">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2.5 text-sm font-medium ${isActive ? 'bg-primary-50 text-primary-600' : 'text-gray-600 hover:bg-gray-50'}`
                }
              >
                {link.label}
              </NavLink>
            ))}
            <Link
              to="/login"
              onClick={() => setOpen(false)}
              aria-label="Đăng nhập"
              className="mt-2 flex items-center justify-center gap-2 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-400 hover:bg-gray-50 hover:text-gray-600"
            >
              <LogIn size={16} />
            </Link>
          </nav>
        </div>
      )}
    </header>
  )
}

export default PublicNavbar
