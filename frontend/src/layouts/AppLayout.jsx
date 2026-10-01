import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  LayoutDashboard, Users, Calendar, FileText, Pill,
  Stethoscope, CreditCard, User, LogOut, Menu, X, ClipboardList
} from 'lucide-react'
import { useState } from 'react'

const navByRole = {
  PATIENT: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/doctors', label: 'Find Doctors', icon: Stethoscope },
    { to: '/book-appointment', label: 'Book Appointment', icon: Calendar },
    { to: '/appointments', label: 'My Appointments', icon: ClipboardList },
    { to: '/medical-records', label: 'Medical Records', icon: FileText },
    { to: '/prescriptions', label: 'Prescriptions', icon: Pill },
    { to: '/billing', label: 'Billing', icon: CreditCard },
    { to: '/profile', label: 'Profile', icon: User },
  ],
  DOCTOR: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/doctor-dashboard', label: 'My Schedule', icon: Calendar },
    { to: '/appointments', label: 'Appointments', icon: ClipboardList },
    { to: '/medical-records', label: 'Patient Records', icon: FileText },
    { to: '/prescriptions', label: 'Prescriptions', icon: Pill },
    { to: '/profile', label: 'Profile', icon: User },
  ],
  ADMIN: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/doctors', label: 'Doctors', icon: Stethoscope },
    { to: '/appointments', label: 'Appointments', icon: ClipboardList },
    { to: '/billing', label: 'Billing', icon: CreditCard },
    { to: '/profile', label: 'Profile', icon: User },
  ],
  RECEPTIONIST: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/doctors', label: 'Doctors', icon: Stethoscope },
    { to: '/appointments', label: 'Appointments', icon: ClipboardList },
    { to: '/billing', label: 'Billing', icon: CreditCard },
    { to: '/profile', label: 'Profile', icon: User },
  ],
}

export default function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const nav = navByRole[user?.role] || navByRole.PATIENT

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-surface-50 flex">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/30 z-40 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed lg:static inset-y-0 left-0 z-50 w-64 bg-white border-r border-surface-200
        transform transition-transform duration-200 ease-in-out
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        <div className="flex flex-col h-full">
          <div className="flex items-center justify-between px-6 py-5 border-b border-surface-100">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-primary-600 flex items-center justify-center">
                <Stethoscope className="w-4 h-4 text-white" />
              </div>
              <span className="font-semibold text-surface-900 tracking-tight">MediCare</span>
            </div>
            <button className="lg:hidden p-1" onClick={() => setSidebarOpen(false)}>
              <X className="w-5 h-5 text-surface-500" />
            </button>
          </div>

          <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
            {nav.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-surface-600 hover:bg-surface-50 hover:text-surface-900'
                  }`
                }
              >
                <Icon className="w-4.5 h-4.5 shrink-0" strokeWidth={1.75} />
                {label}
              </NavLink>
            ))}
          </nav>

          <div className="px-3 py-4 border-t border-surface-100">
            <div className="px-3 py-2 mb-2">
              <p className="text-sm font-medium text-surface-900 truncate">{user?.fullName}</p>
              <p className="text-xs text-surface-500 truncate">{user?.role}</p>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-surface-600 hover:bg-surface-50 hover:text-red-600 transition-colors"
            >
              <LogOut className="w-4.5 h-4.5" strokeWidth={1.75} />
              Sign out
            </button>
          </div>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-surface-200 px-4 lg:px-8 h-14 flex items-center gap-4">
          <button className="lg:hidden p-1.5 -ml-1" onClick={() => setSidebarOpen(true)}>
            <Menu className="w-5 h-5 text-surface-600" />
          </button>
          <div className="flex-1" />
          <span className="text-sm text-surface-500 hidden sm:block">{user?.email}</span>
        </header>

        <main className="flex-1 p-4 lg:p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
