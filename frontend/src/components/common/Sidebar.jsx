import { NavLink } from 'react-router-dom'

function Sidebar({ items }) {
  return (
    <aside className="w-56 shrink-0 border-r border-gray-100 bg-white p-4">
      <nav className="flex flex-col gap-1">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive ? 'bg-primary-50 text-primary-600' : 'text-gray-600 hover:bg-gray-50'
              }`
            }
          >
            <item.icon size={18} />
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}

export default Sidebar
