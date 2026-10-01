import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { format } from 'date-fns'

const statusColor = {
  SCHEDULED: 'bg-blue-50 text-blue-700',
  CONFIRMED: 'bg-emerald-50 text-emerald-700',
  IN_PROGRESS: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-surface-100 text-surface-600',
  CANCELLED: 'bg-red-50 text-red-700',
}

export default function DoctorDashboardPage() {
  const { user } = useAuth()
  const [date, setDate] = useState(format(new Date(), 'yyyy-MM-dd'))
  const [schedule, setSchedule] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (user?.role !== 'DOCTOR') return
    setLoading(true)
    api.get('/appointments/schedule', { params: { date } })
      .then(({ data }) => setSchedule(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load schedule'))
      .finally(() => setLoading(false))
  }, [date, user])

  if (user?.role !== 'DOCTOR') {
    return <div className="card p-8 text-center text-surface-500">Doctor access only</div>
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">My Schedule</h1>
          <p className="text-surface-500 text-sm mt-1">Daily appointments and patient list</p>
        </div>
        <input
          type="date"
          className="input w-auto"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
      </div>

      {loading && (
        <div className="flex justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
        </div>
      )}
      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}
      {!loading && !error && schedule.length === 0 && (
        <div className="card p-12 text-center text-surface-400 text-sm">
          No appointments scheduled for this day
        </div>
      )}

      {!loading && schedule.length > 0 && (
        <div className="card divide-y divide-surface-100">
          {schedule.map((a) => (
            <div key={a.id} className="p-5 flex items-center gap-4">
              <div className="text-center shrink-0 w-16">
                <p className="text-sm font-semibold text-surface-900 tabular-nums">{a.startTime?.slice(0, 5)}</p>
                <p className="text-xs text-surface-400">{a.endTime?.slice(0, 5)}</p>
              </div>
              <div className="w-px h-10 bg-surface-200" />
              <div className="flex-1 min-w-0">
                <p className="font-medium text-surface-900">{a.patientName}</p>
                {a.reason && <p className="text-sm text-surface-500 truncate">{a.reason}</p>}
              </div>
              <span className={`badge ${statusColor[a.status] || ''}`}>
                {a.status?.replace(/_/g, ' ')}
              </span>
              <Link
                to="/medical-records"
                className="text-sm text-primary-600 hover:text-primary-700 shrink-0"
              >
                Records
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
