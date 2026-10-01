import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Stethoscope } from 'lucide-react'

const SPECIALTIES = [
  'GENERAL_PRACTICE', 'CARDIOLOGY', 'DERMATOLOGY', 'ENDOCRINOLOGY', 'GASTROENTEROLOGY',
  'NEUROLOGY', 'ONCOLOGY', 'ORTHOPEDICS', 'PEDIATRICS', 'PSYCHIATRY', 'RADIOLOGY',
  'SURGERY', 'UROLOGY', 'GYNECOLOGY', 'OPHTHALMOLOGY', 'ENT', 'PULMONOLOGY',
]

export default function RegisterPage() {
  const [form, setForm] = useState({
    email: '', password: '', confirmPassword: '', firstName: '', lastName: '',
    phone: '', role: 'PATIENT', dateOfBirth: '', gender: '', specialty: '', licenseNumber: '',
  })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }))

  const validate = () => {
    const errs = {}
    if (!form.email) errs.email = 'Email is required'
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Invalid email'
    if (!form.password || form.password.length < 8) errs.password = 'Min 8 characters'
    else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(form.password))
      errs.password = 'Need upper, lower, digit & special char'
    if (form.password !== form.confirmPassword) errs.confirmPassword = 'Passwords do not match'
    if (!form.firstName) errs.firstName = 'Required'
    if (!form.lastName) errs.lastName = 'Required'
    if (form.role === 'DOCTOR') {
      if (!form.specialty) errs.specialty = 'Required'
      if (!form.licenseNumber) errs.licenseNumber = 'Required'
    }
    setFieldErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!validate()) return
    setLoading(true)
    try {
      const payload = {
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        phone: form.phone || undefined,
        role: form.role,
      }
      if (form.role === 'PATIENT') {
        if (form.dateOfBirth) payload.dateOfBirth = form.dateOfBirth
        if (form.gender) payload.gender = form.gender
      }
      if (form.role === 'DOCTOR') {
        payload.specialty = form.specialty
        payload.licenseNumber = form.licenseNumber
      }
      await register(payload)
      navigate('/')
    } catch (err) {
      const data = err.response?.data
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors)
      else setError(data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="w-full max-w-lg">
        <div className="flex items-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-lg bg-primary-600 flex items-center justify-center">
            <Stethoscope className="w-4 h-4 text-white" />
          </div>
          <span className="font-semibold text-lg">MediCare</span>
        </div>

        <div className="card p-6 sm:p-8">
          <h2 className="text-2xl font-semibold text-surface-900 mb-1">Create account</h2>
          <p className="text-surface-500 text-sm mb-6">Register as a patient or doctor</p>

          {error && (
            <div className="mb-5 p-3.5 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">First name</label>
                <input className="input" value={form.firstName} onChange={(e) => set('firstName', e.target.value)} />
                {fieldErrors.firstName && <p className="text-xs text-red-600 mt-1">{fieldErrors.firstName}</p>}
              </div>
              <div>
                <label className="label">Last name</label>
                <input className="input" value={form.lastName} onChange={(e) => set('lastName', e.target.value)} />
                {fieldErrors.lastName && <p className="text-xs text-red-600 mt-1">{fieldErrors.lastName}</p>}
              </div>
            </div>

            <div>
              <label className="label">Email</label>
              <input type="email" className="input" value={form.email} onChange={(e) => set('email', e.target.value)} />
              {fieldErrors.email && <p className="text-xs text-red-600 mt-1">{fieldErrors.email}</p>}
            </div>

            <div>
              <label className="label">Phone</label>
              <input className="input" value={form.phone} onChange={(e) => set('phone', e.target.value)} />
            </div>

            <div>
              <label className="label">Role</label>
              <select className="input" value={form.role} onChange={(e) => set('role', e.target.value)}>
                <option value="PATIENT">Patient</option>
                <option value="DOCTOR">Doctor</option>
              </select>
            </div>

            {form.role === 'PATIENT' && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">Date of birth</label>
                  <input type="date" className="input" value={form.dateOfBirth} onChange={(e) => set('dateOfBirth', e.target.value)} />
                </div>
                <div>
                  <label className="label">Gender</label>
                  <select className="input" value={form.gender} onChange={(e) => set('gender', e.target.value)}>
                    <option value="">Select</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>
            )}

            {form.role === 'DOCTOR' && (
              <>
                <div>
                  <label className="label">Specialty</label>
                  <select className="input" value={form.specialty} onChange={(e) => set('specialty', e.target.value)}>
                    <option value="">Select specialty</option>
                    {SPECIALTIES.map((s) => (
                      <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
                    ))}
                  </select>
                  {fieldErrors.specialty && <p className="text-xs text-red-600 mt-1">{fieldErrors.specialty}</p>}
                </div>
                <div>
                  <label className="label">License number</label>
                  <input className="input" value={form.licenseNumber} onChange={(e) => set('licenseNumber', e.target.value)} />
                  {fieldErrors.licenseNumber && <p className="text-xs text-red-600 mt-1">{fieldErrors.licenseNumber}</p>}
                </div>
              </>
            )}

            <div>
              <label className="label">Password</label>
              <input type="password" className="input" value={form.password} onChange={(e) => set('password', e.target.value)} />
              {fieldErrors.password && <p className="text-xs text-red-600 mt-1">{fieldErrors.password}</p>}
            </div>
            <div>
              <label className="label">Confirm password</label>
              <input type="password" className="input" value={form.confirmPassword} onChange={(e) => set('confirmPassword', e.target.value)} />
              {fieldErrors.confirmPassword && <p className="text-xs text-red-600 mt-1">{fieldErrors.confirmPassword}</p>}
            </div>

            <button type="submit" className="btn-primary w-full mt-2" disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>
        </div>

        <p className="mt-6 text-center text-sm text-surface-500">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 font-medium hover:text-primary-700">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
