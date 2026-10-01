import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

export default function MedicalRecordsPage() {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [labs, setLabs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [tab, setTab] = useState('visits')
  const [selected, setSelected] = useState(null)
  // Doctor: create form
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    patientId: '', visitDate: new Date().toISOString().slice(0, 10),
    chiefComplaint: '', diagnosis: '', diagnosisCode: '', symptoms: '',
    vitalSigns: '', examinationNotes: '', treatmentPlan: '', followUpDate: '', followUpNotes: '',
  })
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  const patientId = user?.patientId

  useEffect(() => {
    if (!patientId && user?.role === 'PATIENT') {
      setError('Patient profile not found')
      setLoading(false)
      return
    }
    if (user?.role === 'PATIENT' && patientId) {
      Promise.all([
        api.get(`/medical-records/patient/${patientId}`),
        api.get(`/medical-records/patient/${patientId}/lab-results`),
      ])
        .then(([r, l]) => {
          setRecords(r.data)
          setLabs(l.data)
        })
        .catch((err) => setError(err.response?.data?.message || 'Failed to load records'))
        .finally(() => setLoading(false))
    } else {
      // Doctor/admin: empty until they search by patient - show create form
      setLoading(false)
    }
  }, [patientId, user])

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError('')
    if (!form.patientId) {
      setFormError('Patient ID is required')
      return
    }
    setSaving(true)
    try {
      const payload = { ...form, patientId: Number(form.patientId) }
      if (!payload.followUpDate) delete payload.followUpDate
      await api.post('/medical-records', payload)
      setShowForm(false)
      setForm({ ...form, chiefComplaint: '', diagnosis: '', symptoms: '', vitalSigns: '', examinationNotes: '', treatmentPlan: '' })
      alert('Medical record created')
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-16">
        <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
      </div>
    )
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Medical Records</h1>
          <p className="text-surface-500 text-sm mt-1">Visit history, diagnoses, and lab results</p>
        </div>
        {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && (
          <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'New record'}
          </button>
        )}
      </div>

      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}

      {showForm && (
        <form onSubmit={handleCreate} className="card p-6 space-y-4">
          <h3 className="font-medium text-surface-900">Create medical record</h3>
          {formError && <p className="text-sm text-red-600">{formError}</p>}
          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Patient ID</label>
              <input className="input" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} required />
            </div>
            <div>
              <label className="label">Visit date</label>
              <input type="date" className="input" value={form.visitDate} onChange={(e) => setForm({ ...form, visitDate: e.target.value })} required />
            </div>
          </div>
          <div>
            <label className="label">Chief complaint</label>
            <input className="input" value={form.chiefComplaint} onChange={(e) => setForm({ ...form, chiefComplaint: e.target.value })} />
          </div>
          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Diagnosis</label>
              <input className="input" value={form.diagnosis} onChange={(e) => setForm({ ...form, diagnosis: e.target.value })} />
            </div>
            <div>
              <label className="label">ICD-10 code</label>
              <input className="input" value={form.diagnosisCode} onChange={(e) => setForm({ ...form, diagnosisCode: e.target.value })} />
            </div>
          </div>
          <div>
            <label className="label">Symptoms</label>
            <textarea className="input min-h-[60px]" value={form.symptoms} onChange={(e) => setForm({ ...form, symptoms: e.target.value })} />
          </div>
          <div>
            <label className="label">Vital signs</label>
            <input className="input" placeholder="BP, HR, Temp, SpO2..." value={form.vitalSigns} onChange={(e) => setForm({ ...form, vitalSigns: e.target.value })} />
          </div>
          <div>
            <label className="label">Examination notes</label>
            <textarea className="input min-h-[60px]" value={form.examinationNotes} onChange={(e) => setForm({ ...form, examinationNotes: e.target.value })} />
          </div>
          <div>
            <label className="label">Treatment plan</label>
            <textarea className="input min-h-[60px]" value={form.treatmentPlan} onChange={(e) => setForm({ ...form, treatmentPlan: e.target.value })} />
          </div>
          <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save record'}</button>
        </form>
      )}

      {user?.role === 'PATIENT' && (
        <>
          <div className="flex gap-1 border-b border-surface-200">
            {['visits', 'labs'].map((t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition-colors ${
                  tab === t ? 'border-primary-600 text-primary-700' : 'border-transparent text-surface-500 hover:text-surface-700'
                }`}
              >
                {t === 'visits' ? 'Visit history' : 'Lab results'}
              </button>
            ))}
          </div>

          {tab === 'visits' && (
            records.length === 0 ? (
              <div className="card p-12 text-center text-surface-400 text-sm">No visit records yet</div>
            ) : (
              <div className="space-y-3">
                {records.map((r) => (
                  <div key={r.id} className="card p-5 cursor-pointer hover:border-primary-200" onClick={() => setSelected(selected?.id === r.id ? null : r)}>
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-medium text-surface-900">{r.visitDate}</p>
                        <p className="text-sm text-surface-500">Dr. {r.doctorName}</p>
                      </div>
                      {r.diagnosis && <span className="text-sm text-surface-600 max-w-xs truncate">{r.diagnosis}</span>}
                    </div>
                    {selected?.id === r.id && (
                      <div className="mt-4 pt-4 border-t border-surface-100 space-y-2 text-sm">
                        {r.chiefComplaint && <p><span className="text-surface-500">Complaint:</span> {r.chiefComplaint}</p>}
                        {r.diagnosis && <p><span className="text-surface-500">Diagnosis:</span> {r.diagnosis} {r.diagnosisCode && `(${r.diagnosisCode})`}</p>}
                        {r.symptoms && <p><span className="text-surface-500">Symptoms:</span> {r.symptoms}</p>}
                        {r.vitalSigns && <p><span className="text-surface-500">Vitals:</span> {r.vitalSigns}</p>}
                        {r.examinationNotes && <p><span className="text-surface-500">Exam:</span> {r.examinationNotes}</p>}
                        {r.treatmentPlan && <p><span className="text-surface-500">Treatment:</span> {r.treatmentPlan}</p>}
                        {r.followUpDate && <p><span className="text-surface-500">Follow-up:</span> {r.followUpDate}</p>}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )
          )}

          {tab === 'labs' && (
            labs.length === 0 ? (
              <div className="card p-12 text-center text-surface-400 text-sm">No lab results yet</div>
            ) : (
              <div className="card overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-surface-100 text-left text-surface-500">
                      <th className="px-5 py-3 font-medium">Test</th>
                      <th className="px-5 py-3 font-medium">Result</th>
                      <th className="px-5 py-3 font-medium">Range</th>
                      <th className="px-5 py-3 font-medium">Date</th>
                      <th className="px-5 py-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-100">
                    {labs.map((l) => (
                      <tr key={l.id}>
                        <td className="px-5 py-3 font-medium text-surface-900">{l.testName}</td>
                        <td className="px-5 py-3">{l.resultValue} {l.unit}</td>
                        <td className="px-5 py-3 text-surface-500">{l.referenceRange || '—'}</td>
                        <td className="px-5 py-3 text-surface-500">{l.testDate}</td>
                        <td className="px-5 py-3">
                          <span className={`badge ${l.isAbnormal ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'}`}>
                            {l.isAbnormal ? 'Abnormal' : 'Normal'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          )}
        </>
      )}

      {user?.role !== 'PATIENT' && !showForm && (
        <div className="card p-8 text-center text-surface-500 text-sm">
          Use “New record” to create a medical record for a patient (enter their Patient ID).
        </div>
      )}
    </div>
  )
}
