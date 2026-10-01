import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { format, addDays } from 'date-fns'

export default function BookAppointmentPage() {
  const { user } = useAuth()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [doctors, setDoctors] = useState([])
  const [doctorId, setDoctorId] = useState(params.get('doctorId') || '')
  const [date, setDate] = useState(format(addDays(new Date(), 1), 'yyyy-MM-dd'))
  const [slots, setSlots] = useState([])
  const [selectedSlot, setSelectedSlot] = useState(null)
  const [reason, setReason] = useState('')
  const [loading, setLoading] = useState(false)
  const [slotsLoading, setSlotsLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(null)

  useEffect(() => {
    if (user?.role !== 'PATIENT') return
    api.get('/doctors', { params: { availableOnly: true } })
      .then(({ data }) => setDoctors(data))
      .catch(() => setError('Failed to load doctors'))
  }, [user])

  useEffect(() => {
    if (!doctorId || !date) {
      setSlots([])
      return
    }
    setSlotsLoading(true)
    setSelectedSlot(null)
    api.get(`/doctors/${doctorId}/slots`, { params: { date } })
      .then(({ data }) => setSlots(data))
      .catch(() => setSlots([]))
      .finally(() => setSlotsLoading(false))
  }, [doctorId, date])

  const handleBook = async (e) => {
    e.preventDefault()
    setError('')
    if (!doctorId || !selectedSlot) {
      setError('Please select a doctor and time slot')
      return
    }
    setLoading(true)
    try {
      const { data } = await api.post('/appointments', {
        doctorId: Number(doctorId),
        appointmentDate: date,
        startTime: selectedSlot.startTime,
        reason: reason || undefined,
      })
      setSuccess(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed')
    } finally {
      setLoading(false)
    }
  }

  if (user?.role !== 'PATIENT') {
    return (
      <div className="card p-8 text-center text-surface-500">
        Only patients can book appointments from this page.
      </div>
    )
  }

  if (success) {
    return (
      <div className="max-w-lg mx-auto card p-8 text-center space-y-4">
        <div className="w-12 h-12 rounded-full bg-emerald-50 flex items-center justify-center mx-auto">
          <span className="text-emerald-600 text-xl">✓</span>
        </div>
        <h2 className="text-xl font-semibold text-surface-900">Appointment booked</h2>
        <p className="text-sm text-surface-500">
          {success.doctorName} on {success.appointmentDate} at {success.startTime?.slice(0, 5)}
        </p>
        <div className="flex gap-3 justify-center pt-2">
          <button className="btn-primary" onClick={() => navigate('/appointments')}>View appointments</button>
          <button className="btn-secondary" onClick={() => { setSuccess(null); setSelectedSlot(null) }}>Book another</button>
        </div>
      </div>
    )
  }

  const minDate = format(new Date(), 'yyyy-MM-dd')

  return (
    <div className="max-w-xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Book Appointment</h1>
        <p className="text-surface-500 text-sm mt-1">Select a doctor, date, and available time slot</p>
      </div>

      {error && (
        <div className="p-3.5 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{error}</div>
      )}

      <form onSubmit={handleBook} className="card p-6 space-y-5">
        <div>
          <label className="label">Doctor</label>
          <select className="input" value={doctorId} onChange={(e) => setDoctorId(e.target.value)} required>
            <option value="">Select a doctor</option>
            {doctors.map((d) => (
              <option key={d.id} value={d.id}>
                {d.fullName} — {d.specialty?.replace(/_/g, ' ')}
                {d.consultationFee != null ? ` ($${Number(d.consultationFee).toFixed(0)})` : ''}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="label">Date</label>
          <input type="date" className="input" value={date} min={minDate} onChange={(e) => setDate(e.target.value)} required />
        </div>

        <div>
          <label className="label">Available time slots</label>
          {slotsLoading ? (
            <div className="py-6 flex justify-center">
              <div className="animate-spin rounded-full h-6 w-6 border-2 border-primary-600 border-t-transparent" />
            </div>
          ) : slots.length === 0 ? (
            <p className="text-sm text-surface-400 py-4">
              {doctorId ? 'No slots available for this date' : 'Select a doctor and date'}
            </p>
          ) : (
            <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
              {slots.map((s) => (
                <button
                  key={s.startTime}
                  type="button"
                  disabled={!s.available}
                  onClick={() => setSelectedSlot(s)}
                  className={`
                    py-2 px-2 rounded-lg text-sm font-medium border transition-colors
                    ${!s.available
                      ? 'bg-surface-50 text-surface-300 border-surface-100 cursor-not-allowed'
                      : selectedSlot?.startTime === s.startTime
                        ? 'bg-primary-600 text-white border-primary-600'
                        : 'bg-white text-surface-700 border-surface-200 hover:border-primary-400'
                    }
                  `}
                >
                  {s.startTime?.slice(0, 5)}
                </button>
              ))}
            </div>
          )}
        </div>

        <div>
          <label className="label">Reason (optional)</label>
          <textarea
            className="input min-h-[80px] resize-y"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Brief description of the visit reason"
          />
        </div>

        <button type="submit" className="btn-primary w-full" disabled={loading || !selectedSlot}>
          {loading ? 'Booking…' : 'Confirm appointment'}
        </button>
      </form>
    </div>
  )
}
