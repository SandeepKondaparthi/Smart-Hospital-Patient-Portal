import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'
import { Search, Stethoscope } from 'lucide-react'

const SPECIALTIES = [
  '', 'GENERAL_PRACTICE', 'CARDIOLOGY', 'DERMATOLOGY', 'ENDOCRINOLOGY', 'GASTROENTEROLOGY',
  'NEUROLOGY', 'ONCOLOGY', 'ORTHOPEDICS', 'PEDIATRICS', 'PSYCHIATRY', 'RADIOLOGY',
  'SURGERY', 'UROLOGY', 'GYNECOLOGY', 'OPHTHALMOLOGY', 'ENT', 'PULMONOLOGY',
]

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState([])
  const [specialty, setSpecialty] = useState('')
  const [availableOnly, setAvailableOnly] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    setError('')
    const params = {}
    if (specialty) params.specialty = specialty
    if (availableOnly) params.availableOnly = true
    api.get('/doctors', { params })
      .then(({ data }) => setDoctors(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load doctors'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [specialty, availableOnly])

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Doctor Directory</h1>
        <p className="text-surface-500 text-sm mt-1">Search and filter by specialty and availability</p>
      </div>

      <div className="card p-4 flex flex-col sm:flex-row gap-3">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-surface-400" />
          <select
            className="input pl-9"
            value={specialty}
            onChange={(e) => setSpecialty(e.target.value)}
          >
            <option value="">All specialties</option>
            {SPECIALTIES.filter(Boolean).map((s) => (
              <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
            ))}
          </select>
        </div>
        <label className="flex items-center gap-2 text-sm text-surface-600 px-2 cursor-pointer">
          <input
            type="checkbox"
            checked={availableOnly}
            onChange={(e) => setAvailableOnly(e.target.checked)}
            className="rounded border-surface-300 text-primary-600 focus:ring-primary-500"
          />
          Available only
        </label>
      </div>

      {loading && (
        <div className="flex justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
        </div>
      )}

      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}

      {!loading && !error && doctors.length === 0 && (
        <div className="card p-12 text-center text-surface-400 text-sm">
          No doctors found matching your filters
        </div>
      )}

      {!loading && doctors.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2">
          {doctors.map((d) => (
            <div key={d.id} className="card p-5 flex gap-4">
              <div className="w-12 h-12 rounded-full bg-primary-50 flex items-center justify-center shrink-0">
                <Stethoscope className="w-5 h-5 text-primary-600" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="font-medium text-surface-900">{d.fullName}</h3>
                    <p className="text-sm text-primary-600">{d.specialty?.replace(/_/g, ' ')}</p>
                  </div>
                  <span className={`badge shrink-0 ${d.isAvailable ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-100 text-surface-500'}`}>
                    {d.isAvailable ? 'Available' : 'Unavailable'}
                  </span>
                </div>
                {d.department && <p className="text-xs text-surface-500 mt-1">{d.department}</p>}
                {d.yearsOfExperience != null && (
                  <p className="text-xs text-surface-500">{d.yearsOfExperience} years experience</p>
                )}
                {d.consultationFee != null && (
                  <p className="text-sm text-surface-700 mt-2">
                    Consultation: <span className="font-medium">${Number(d.consultationFee).toFixed(2)}</span>
                  </p>
                )}
                {d.bio && <p className="text-xs text-surface-500 mt-2 line-clamp-2">{d.bio}</p>}
                <div className="mt-3">
                  <Link
                    to={`/book-appointment?doctorId=${d.id}`}
                    className={`btn-primary text-xs py-2 ${!d.isAvailable ? 'opacity-50 pointer-events-none' : ''}`}
                  >
                    Book appointment
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
