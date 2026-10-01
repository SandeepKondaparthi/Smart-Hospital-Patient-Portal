import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { Calendar, Users, Stethoscope, CreditCard, Clock, ArrowRight } from 'lucide-react'
import { format } from 'date-fns'

const statusColor = {
  SCHEDULED: 'bg-blue-50 text-blue-700',
  CONFIRMED: 'bg-emerald-50 text-emerald-700',
  IN_PROGRESS: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-surface-100 text-surface-600',
  CANCELLED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-red-50 text-red-600',
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/dashboard')
      .then(({ data }) => setStats(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load dashboard'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="card p-6 text-center text-red-600">{error}</div>
    )
  }

  const cards = [
    { label: 'Total Patients', value: stats?.totalPatients ?? 0, icon: Users, show: user?.role !== 'PATIENT' },
    { label: 'Total Doctors', value: stats?.totalDoctors ?? 0, icon: Stethoscope, show: user?.role !== 'PATIENT' },
    { label: "Today's Appointments", value: stats?.todayAppointments ?? 0, icon: Calendar, show: true },
    { label: 'Upcoming', value: stats?.upcomingAppointments ?? 0, icon: Clock, show: true },
    { label: 'Pending Bills', value: stats?.pendingBills ?? 0, icon: CreditCard, show: user?.role !== 'DOCTOR' },
  ].filter((c) => c.show)

  const appointments = stats?.todaySchedule?.length
    ? stats.todaySchedule
    : stats?.recentAppointments || []

  return (
    <div className="space-y-8 max-w-6xl">
      <div>
        <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">
          Welcome back, {user?.firstName}
        </h1>
        <p className="text-surface-500 text-sm mt-1">
          {format(new Date(), 'EEEE, MMMM d, yyyy')}
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(({ label, value, icon: Icon }) => (
          <div key={label} className="card p-5">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm text-surface-500">{label}</span>
              <div className="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
                <Icon className="w-4 h-4 text-primary-600" strokeWidth={1.75} />
              </div>
            </div>
            <p className="text-2xl font-semibold text-surface-900 tabular-nums">{value}</p>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-100">
          <h2 className="font-medium text-surface-900">
            {user?.role === 'DOCTOR' ? "Today's Schedule" : 'Recent Appointments'}
          </h2>
          <Link to="/appointments" className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
            View all <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
        {appointments.length === 0 ? (
          <div className="px-5 py-12 text-center text-surface-400 text-sm">
            No appointments to show
          </div>
        ) : (
          <ul className="divide-y divide-surface-100">
            {appointments.map((a) => (
              <li key={a.id} className="px-5 py-4 flex items-center gap-4 hover:bg-surface-50/50">
                <div className="w-10 h-10 rounded-full bg-primary-50 flex items-center justify-center text-primary-700 text-sm font-medium shrink-0">
                  {(user?.role === 'DOCTOR' ? a.patientName : a.doctorName)?.charAt(0) || '?'}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-surface-900 truncate">
                    {user?.role === 'DOCTOR' ? a.patientName : a.doctorName}
                  </p>
                  <p className="text-xs text-surface-500">
                    {a.appointmentDate} · {a.startTime?.slice(0, 5)} – {a.endTime?.slice(0, 5)}
                    {a.doctorSpecialty && ` · ${a.doctorSpecialty.replace(/_/g, ' ')}`}
                  </p>
                </div>
                <span className={`badge ${statusColor[a.status] || 'bg-surface-100 text-surface-600'}`}>
                  {a.status?.replace(/_/g, ' ')}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {user?.role === 'PATIENT' && (
        <div className="flex flex-wrap gap-3">
          <Link to="/book-appointment" className="btn-primary">Book Appointment</Link>
          <Link to="/doctors" className="btn-secondary">Find a Doctor</Link>
        </div>
      )}
    </div>
  )
}
