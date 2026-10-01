import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'
import { Stethoscope, ArrowLeft } from 'lucide-react'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!email) {
      setError('Email is required')
      return
    }
    setLoading(true)
    try {
      await api.post('/auth/forgot-password', { email })
      setSent(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong')
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
          {sent ? (
            <div className="text-center">
              <h2 className="text-xl font-semibold text-surface-900 mb-2">Check your email</h2>
              <p className="text-surface-500 text-sm mb-6">
                If an account exists for <strong>{email}</strong>, we've sent a password reset link.
                The link expires in 1 hour.
              </p>
              <p className="text-xs text-surface-400 mb-4">
                In development, the reset token is also logged on the backend console.
              </p>
              <Link to="/login" className="btn-primary">Back to sign in</Link>
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-semibold text-surface-900 mb-1">Forgot password</h2>
              <p className="text-surface-500 text-sm mb-6">
                Enter your email and we'll send a reset link.
              </p>
              {error && (
                <div className="mb-5 p-3.5 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{error}</div>
              )}
              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label className="label" htmlFor="email">Email</label>
                  <input
                    id="email"
                    type="email"
                    className="input"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                  />
                </div>
                <button type="submit" className="btn-primary w-full" disabled={loading}>
                  {loading ? 'Sending…' : 'Send reset link'}
                </button>
              </form>
            </>
          )}
        </div>

        <Link to="/login" className="mt-6 flex items-center justify-center gap-1.5 text-sm text-surface-500 hover:text-surface-700">
          <ArrowLeft className="w-4 h-4" /> Back to sign in
        </Link>
      </div>
    </div>
  )
}
