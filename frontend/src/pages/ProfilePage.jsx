import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

export default function ProfilePage() {
  const { user, updateUser, logout } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phone: user?.phone || '',
  })
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)
  const [pwLoading, setPwLoading] = useState(false)

  const handleProfile = async (e) => {
    e.preventDefault()
    setMsg('')
    setErr('')
    setLoading(true)
    try {
      const { data } = await api.put('/auth/profile', form)
      updateUser(data)
      setMsg('Profile updated successfully')
    } catch (error) {
      setErr(error.response?.data?.message || 'Update failed')
    } finally {
      setLoading(false)
    }
  }

  const handlePassword = async (e) => {
    e.preventDefault()
    setMsg('')
    setErr('')
    if (pwForm.newPassword !== pwForm.confirm) {
      setErr('New passwords do not match')
      return
    }
    setPwLoading(true)
    try {
      await api.post('/auth/change-password', {
        currentPassword: pwForm.currentPassword,
        newPassword: pwForm.newPassword,
      })
      setMsg('Password changed successfully')
      setPwForm({ currentPassword: '', newPassword: '', confirm: '' })
    } catch (error) {
      setErr(error.response?.data?.message || 'Password change failed')
    } finally {
      setPwLoading(false)
    }
  }

  const handleSignOut = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="max-w-xl space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Account Settings</h1>
        <p className="text-surface-500 text-sm mt-1">Update your profile and password</p>
      </div>

      {msg && <div className="p-3.5 rounded-lg bg-emerald-50 border border-emerald-100 text-emerald-700 text-sm">{msg}</div>}
      {err && <div className="p-3.5 rounded-lg bg-red-50 border border-red-100 text-red-700 text-sm">{err}</div>}

      <form onSubmit={handleProfile} className="card p-6 space-y-4">
        <h2 className="font-medium text-surface-900">Profile</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">First name</label>
            <input className="input" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
          </div>
          <div>
            <label className="label">Last name</label>
            <input className="input" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          </div>
        </div>
        <div>
          <label className="label">Email</label>
          <input type="email" className="input" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </div>
        <div>
          <label className="label">Phone</label>
          <input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div className="text-sm text-surface-500">
          Role: <span className="font-medium text-surface-700">{user?.role}</span>
          {user?.patientId && <> · Patient ID: {user.patientId}</>}
          {user?.doctorId && <> · Doctor ID: {user.doctorId}</>}
        </div>
        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Saving…' : 'Save changes'}
        </button>
      </form>

      <form onSubmit={handlePassword} className="card p-6 space-y-4">
        <h2 className="font-medium text-surface-900">Change password</h2>
        <div>
          <label className="label">Current password</label>
          <input type="password" className="input" value={pwForm.currentPassword} onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })} required />
        </div>
        <div>
          <label className="label">New password</label>
          <input type="password" className="input" value={pwForm.newPassword} onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })} required />
        </div>
        <div>
          <label className="label">Confirm new password</label>
          <input type="password" className="input" value={pwForm.confirm} onChange={(e) => setPwForm({ ...pwForm, confirm: e.target.value })} required />
        </div>
        <button type="submit" className="btn-primary" disabled={pwLoading}>
          {pwLoading ? 'Updating…' : 'Update password'}
        </button>
      </form>

      <div className="card p-6">
        <h2 className="font-medium text-surface-900 mb-3">Sign out</h2>
        <p className="text-sm text-surface-500 mb-4">End your session on this device</p>
        <button className="btn-danger" onClick={handleSignOut}>Sign out</button>
      </div>
    </div>
  )
}
