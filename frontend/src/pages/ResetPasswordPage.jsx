import { useState } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { Stethoscope } from 'lucide-react'

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token') || ''
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!token) {
      setError('Invalid or missing reset token')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(password)) {
      setError('Password must contain uppercase, lowercase, digit and special character')
      return
    }
    if (password !== confirm) {
      setError('Passwords do not match')
      return
    }
    setLoading(true)
    try {
      await api.post('/auth/reset-password', { token, newPassword: password })
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="w-full max-w-md">
        <div className="flex items-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-lg bg-primary-600 flex items-center justify-center">
            <Stethoscope className="w-4 h-4 text-white" />
          </div>
          <span className="font-semibold text-lg">MediCare</span>
        </div>

        <div className="card p-6 sm:p-8">
          {success ? (
            <div className="text-center">
              <h2 className="text-xl font-semibold text-surface-900 mb-2">Password reset</h2>
              <p className="text-surface-500 text-sm">Your password has been updated. Redirecting to sign in…</p>
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-semibold text-surface-900 mb-1">Reset password</h2>
              <p className="text-surface-500 text-sm mb-6">Choose a new password for your account</p>
              {error && (
                <div className="mb-5 p-3.5 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{error}</div>
              )}
              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label className="label">New password</label>
                  <input type="password" className="input" value={password} onChange={(e) => setPassword(e.target.value)} />
                </div>
                <div>
                  <label className="label">Confirm password</label>
                  <input type="password" className="input" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
                </div>
                <button type="submit" className="btn-primary w-full" disabled={loading}>
                  {loading ? 'Resetting…' : 'Reset password'}
                </button>
              </form>
            </>
          )}
        </div>
        <p className="mt-6 text-center text-sm">
          <Link to="/login" className="text-primary-600 hover:text-primary-700">Back to sign in</Link>
        </p>
      </div>
    </div>
  )
}
