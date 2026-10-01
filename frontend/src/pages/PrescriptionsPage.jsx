import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

export default function PrescriptionsPage() {
  const { user } = useAuth()
  const [prescriptions, setPrescriptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    patientId: '', notes: '', validUntil: '',
    items: [{ medicationName: '', dosage: '', frequency: '', duration: '', quantity: '', instructions: '', route: 'oral' }],
  })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    if (user?.role === 'PATIENT' && user.patientId) {
      api.get(`/prescriptions/patient/${user.patientId}`)
        .then(({ data }) => setPrescriptions(data))
        .catch((err) => setError(err.response?.data?.message || 'Failed to load'))
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [user])

  const addItem = () => {
    setForm({
      ...form,
      items: [...form.items, { medicationName: '', dosage: '', frequency: '', duration: '', quantity: '', instructions: '', route: 'oral' }],
    })
  }

  const updateItem = (i, key, val) => {
    const items = [...form.items]
    items[i] = { ...items[i], [key]: val }
    setForm({ ...form, items })
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      await api.post('/prescriptions', {
        patientId: Number(form.patientId),
        notes: form.notes || undefined,
        validUntil: form.validUntil || undefined,
        items: form.items.filter((it) => it.medicationName).map((it) => ({
          ...it,
          quantity: it.quantity ? Number(it.quantity) : undefined,
        })),
      })
      setShowForm(false)
      alert('Prescription issued')
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Prescriptions</h1>
          <p className="text-surface-500 text-sm mt-1">Medications and treatment plans</p>
        </div>
        {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && (
          <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'Issue prescription'}
          </button>
        )}
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="card p-6 space-y-4">
          <h3 className="font-medium">New prescription</h3>
          {formError && <p className="text-sm text-red-600">{formError}</p>}
          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Patient ID</label>
              <input className="input" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} required />
            </div>
            <div>
              <label className="label">Valid until</label>
              <input type="date" className="input" value={form.validUntil} onChange={(e) => setForm({ ...form, validUntil: e.target.value })} />
            </div>
          </div>
          <div>
            <label className="label">Notes</label>
            <textarea className="input min-h-[60px]" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <label className="label mb-0">Medications</label>
              <button type="button" className="text-sm text-primary-600" onClick={addItem}>+ Add item</button>
            </div>
            {form.items.map((item, i) => (
              <div key={i} className="grid sm:grid-cols-3 gap-2 p-3 bg-surface-50 rounded-lg">
                <input className="input" placeholder="Medication" value={item.medicationName} onChange={(e) => updateItem(i, 'medicationName', e.target.value)} required />
                <input className="input" placeholder="Dosage" value={item.dosage} onChange={(e) => updateItem(i, 'dosage', e.target.value)} required />
                <input className="input" placeholder="Frequency" value={item.frequency} onChange={(e) => updateItem(i, 'frequency', e.target.value)} required />
                <input className="input" placeholder="Duration" value={item.duration} onChange={(e) => updateItem(i, 'duration', e.target.value)} />
                <input className="input" placeholder="Qty" value={item.quantity} onChange={(e) => updateItem(i, 'quantity', e.target.value)} />
                <input className="input" placeholder="Instructions" value={item.instructions} onChange={(e) => updateItem(i, 'instructions', e.target.value)} />
              </div>
            ))}
          </div>
          <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Issue prescription'}</button>
        </form>
      )}

      {loading && (
        <div className="flex justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
        </div>
      )}
      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}
      {!loading && user?.role === 'PATIENT' && prescriptions.length === 0 && (
        <div className="card p-12 text-center text-surface-400 text-sm">No prescriptions yet</div>
      )}
      {!loading && prescriptions.length > 0 && (
        <div className="space-y-3">
          {prescriptions.map((p) => (
            <div key={p.id} className="card overflow-hidden">
              <button
                className="w-full p-5 flex items-center justify-between text-left hover:bg-surface-50/50"
                onClick={() => setExpanded(expanded === p.id ? null : p.id)}
              >
                <div>
                  <p className="font-medium text-surface-900">{p.prescriptionDate}</p>
                  <p className="text-sm text-surface-500">Dr. {p.doctorName} · {p.items?.length || 0} medication(s)</p>
                </div>
                <span className={`badge ${p.isActive ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-100 text-surface-500'}`}>
                  {p.isActive ? 'Active' : 'Inactive'}
                </span>
              </button>
              {expanded === p.id && (
                <div className="px-5 pb-5 border-t border-surface-100 pt-4">
                  {p.notes && <p className="text-sm text-surface-600 mb-3">{p.notes}</p>}
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-surface-500">
                        <th className="pb-2 font-medium">Medication</th>
                        <th className="pb-2 font-medium">Dosage</th>
                        <th className="pb-2 font-medium">Frequency</th>
                        <th className="pb-2 font-medium">Duration</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-surface-50">
                      {(p.items || []).map((it) => (
                        <tr key={it.id}>
                          <td className="py-2 font-medium">{it.medicationName}</td>
                          <td className="py-2">{it.dosage}</td>
                          <td className="py-2">{it.frequency}</td>
                          <td className="py-2">{it.duration || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
      {user?.role !== 'PATIENT' && !showForm && prescriptions.length === 0 && (
        <div className="card p-8 text-center text-surface-500 text-sm">
          Issue a new prescription for a patient using the button above.
        </div>
      )}
    </div>
  )
}
