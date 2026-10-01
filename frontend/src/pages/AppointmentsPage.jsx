import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

const statusColor = {
  SCHEDULED: 'bg-blue-50 text-blue-700',
  CONFIRMED: 'bg-emerald-50 text-emerald-700',
  IN_PROGRESS: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-surface-100 text-surface-600',
  CANCELLED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-red-50 text-red-600',
}

export default function AppointmentsPage() {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState(null)

  const load = () => {
    setLoading(true)
    api.get('/appointments/me')
      .then(({ data }) => setAppointments(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const updateStatus = async (id, status, notes) => {
    setActionLoading(id)
    try {
      await api.patch(`/appointments/${id}/status`, { status, notes })
      load()
    } catch (err) {
      alert(err.response?.data?.message || 'Update failed')
    } finally {
      setActionLoading(null)
    }
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Appointments</h1>
        <p className="text-surface-500 text-sm mt-1">View and manage your appointments</p>
      </div>

      {loading && (
        <div className="flex justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
        </div>
      )}
      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}
      {!loading && !error && appointments.length === 0 && (
        <div className="card p-12 text-center text-surface-400 text-sm">No appointments found</div>
      )}

      {!loading && appointments.length > 0 && (
        <div className="card divide-y divide-surface-100">
          {appointments.map((a) => (
            <div key={a.id} className="p-5 flex flex-col sm:flex-row sm:items-center gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <p className="font-medium text-surface-900">
                    {user?.role === 'DOCTOR' ? a.patientName : a.doctorName}
                  </p>
                  <span className={`badge ${statusColor[a.status] || ''}`}>
                    {a.status?.replace(/_/g, ' ')}
                  </span>
                </div>
                <p className="text-sm text-surface-500 mt-0.5">
                  {a.appointmentDate} · {a.startTime?.slice(0, 5)} – {a.endTime?.slice(0, 5)}
                  {a.doctorSpecialty && ` · ${a.doctorSpecialty.replace(/_/g, ' ')}`}
                </p>
                {a.reason && <p className="text-sm text-surface-600 mt-1">{a.reason}</p>}
              </div>
              <div className="flex gap-2 flex-wrap">
                {['SCHEDULED', 'CONFIRMED'].includes(a.status) && (
                  <button
                    className="btn-secondary text-xs py-1.5"
                    disabled={actionLoading === a.id}
                    onClick={() => {
                      if (confirm('Cancel this appointment?')) {
                        updateStatus(a.id, 'CANCELLED', 'Cancelled by user')
                      }
                    }}
                  >
                    Cancel
                  </button>
                )}
                {user?.role === 'DOCTOR' && a.status === 'SCHEDULED' && (
                  <button
                    className="btn-primary text-xs py-1.5"
                    disabled={actionLoading === a.id}
                    onClick={() => updateStatus(a.id, 'CONFIRMED')}
                  >
                    Confirm
                  </button>
                )}
                {user?.role === 'DOCTOR' && ['SCHEDULED', 'CONFIRMED', 'IN_PROGRESS'].includes(a.status) && (
                  <button
                    className="btn-primary text-xs py-1.5"
                    disabled={actionLoading === a.id}
                    onClick={() => updateStatus(a.id, 'COMPLETED')}
                  >
                    Complete
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
