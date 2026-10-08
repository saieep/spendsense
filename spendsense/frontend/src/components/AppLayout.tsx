import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { theme } from '../lib/theme'

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/expenses', label: 'Expenses' },
  { to: '/categories', label: 'Categories' },
]

export function AppLayout() {
  const { user, logout } = useAuth()
  const location = useLocation()

  return (
    <div className={`min-h-screen overflow-x-hidden ${theme.page}`}>
      <header className={theme.header}>
        <div className={`${theme.contentMax} flex items-center justify-between px-4 py-4 sm:px-6`}>
          <div className="min-w-0">
            <p className={theme.logo}>SpendSense</p>
            <p className="mt-0.5 truncate text-xs text-slate-400 sm:text-sm">{user?.email}</p>
          </div>
          <button type="button" onClick={logout} className={theme.btnSecondary}>
            Log out
          </button>
        </div>
        <nav className={`${theme.contentMax} flex flex-wrap gap-2 px-4 pb-4 sm:px-6`}>
          {navItems.map((item) => {
            const active = location.pathname === item.to
            return (
              <Link
                key={item.to}
                to={item.to}
                className={active ? theme.navActive : theme.navInactive}
              >
                {item.label}
              </Link>
            )
          })}
        </nav>
      </header>

      <main className={`${theme.contentMax} px-4 py-6 sm:px-6 sm:py-8`}>
        <Outlet />
      </main>
    </div>
  )
}
