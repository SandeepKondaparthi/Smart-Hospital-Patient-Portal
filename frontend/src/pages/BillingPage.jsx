import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

const statusColor = {
  PENDING: 'bg-amber-50 text-amber-700',
  PARTIALLY_PAID: 'bg-blue-50 text-blue-700',
  PAID: 'bg-emerald-50 text-emerald-700',
  OVERDUE: 'bg-red-50 text-red-700',
  CANCELLED: 'bg-surface-100 text-surface-500',
  REFUNDED: 'bg-purple-50 text-purple-700',
}

export default function BillingPage() {
  const { user } = useAuth()
  const [billings, setBillings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null)
  const [payForm, setPayForm] = useState(null)
  const [payAmount, setPayAmount] = useState('')
  const [payMethod, setPayMethod] = useState('CARD')
  const [paying, setPaying] = useState(false)

  // Staff create bill
  const [showCreate, setShowCreate] = useState(false)
  const [createForm, setCreateForm] = useState({
    patientId: '', taxAmount: '0', discountAmount: '0',
    insuranceProvider: '', insurancePolicyNumber: '', notes: '',
    items: [{ description: 'Consultation', quantity: 1, unitPrice: '100', serviceCode: '' }],
  })

  useEffect(() => {
    if (user?.role === 'PATIENT' && user.patientId) {
      api.get(`/billings/patient/${user.patientId}`)
        .then(({ data }) => setBillings(data))
        .catch((err) => setError(err.response?.data?.message || 'Failed to load'))
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [user])

  const recordPayment = async (billingId) => {
    setPaying(true)
    try {
      const { data } = await api.post(`/billings/${billingId}/payments`, {
        amount: Number(payAmount),
        paymentMethod: payMethod,
      })
      setBillings((prev) => prev.map((b) => (b.id === billingId ? data : b)))
      setPayForm(null)
      setPayAmount('')
    } catch (err) {
      alert(err.response?.data?.message || 'Payment failed')
    } finally {
      setPaying(false)
    }
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      await api.post('/billings', {
        patientId: Number(createForm.patientId),
        taxAmount: Number(createForm.taxAmount) || 0,
        discountAmount: Number(createForm.discountAmount) || 0,
        insuranceProvider: createForm.insuranceProvider || undefined,
        insurancePolicyNumber: createForm.insurancePolicyNumber || undefined,
        notes: createForm.notes || undefined,
        items: createForm.items.map((it) => ({
          description: it.description,
          quantity: Number(it.quantity) || 1,
          unitPrice: Number(it.unitPrice),
          serviceCode: it.serviceCode || undefined,
        })),
      })
      setShowCreate(false)
      alert('Invoice created')
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create invoice')
    }
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-surface-900 tracking-tight">Billing & Insurance</h1>
          <p className="text-surface-500 text-sm mt-1">Invoices, claims, and payments</p>
        </div>
        {(user?.role === 'ADMIN' || user?.role === 'RECEPTIONIST' || user?.role === 'DOCTOR') && (
          <button className="btn-primary" onClick={() => setShowCreate(!showCreate)}>
            {showCreate ? 'Cancel' : 'New invoice'}
          </button>
        )}
      </div>

      {showCreate && (
        <form onSubmit={handleCreate} className="card p-6 space-y-4">
          <h3 className="font-medium">Create invoice</h3>
          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label className="label">Patient ID</label>
              <input className="input" value={createForm.patientId} onChange={(e) => setCreateForm({ ...createForm, patientId: e.target.value })} required />
            </div>
            <div>
              <label className="label">Insurance provider</label>
              <input className="input" value={createForm.insuranceProvider} onChange={(e) => setCreateForm({ ...createForm, insuranceProvider: e.target.value })} />
            </div>
          </div>
          {createForm.items.map((it, i) => (
            <div key={i} className="grid sm:grid-cols-3 gap-2">
              <input className="input" placeholder="Description" value={it.description}
                onChange={(e) => {
                  const items = [...createForm.items]
                  items[i] = { ...items[i], description: e.target.value }
                  setCreateForm({ ...createForm, items })
                }} required />
              <input className="input" type="number" placeholder="Qty" value={it.quantity}
                onChange={(e) => {
                  const items = [...createForm.items]
                  items[i] = { ...items[i], quantity: e.target.value }
                  setCreateForm({ ...createForm, items })
                }} />
              <input className="input" type="number" step="0.01" placeholder="Unit price" value={it.unitPrice}
                onChange={(e) => {
                  const items = [...createForm.items]
                  items[i] = { ...items[i], unitPrice: e.target.value }
                  setCreateForm({ ...createForm, items })
                }} required />
            </div>
          ))}
          <button type="submit" className="btn-primary">Create invoice</button>
        </form>
      )}

      {loading && (
        <div className="flex justify-center py-16">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-primary-600 border-t-transparent" />
        </div>
      )}
      {error && <div className="card p-5 text-red-600 text-sm">{error}</div>}
      {!loading && user?.role === 'PATIENT' && billings.length === 0 && (
        <div className="card p-12 text-center text-surface-400 text-sm">No invoices found</div>
      )}

      {!loading && billings.length > 0 && (
        <div className="space-y-3">
          {billings.map((b) => (
            <div key={b.id} className="card overflow-hidden">
              <button
                className="w-full p-5 flex items-center justify-between text-left hover:bg-surface-50/50"
                onClick={() => setExpanded(expanded === b.id ? null : b.id)}
              >
                <div>
                  <p className="font-medium text-surface-900">{b.invoiceNumber}</p>
                  <p className="text-sm text-surface-500">
                    {b.billingDate} · Due {b.dueDate}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-surface-900 tabular-nums">${Number(b.totalAmount).toFixed(2)}</p>
                  <span className={`badge ${statusColor[b.status] || ''}`}>{b.status?.replace(/_/g, ' ')}</span>
                </div>
              </button>

              {expanded === b.id && (
                <div className="px-5 pb-5 border-t border-surface-100 pt-4 space-y-4">
                  <div className="text-sm space-y-1">
                    <p><span className="text-surface-500">Subtotal:</span> ${Number(b.subtotal).toFixed(2)}</p>
                    <p><span className="text-surface-500">Tax:</span> ${Number(b.taxAmount || 0).toFixed(2)}</p>
                    <p><span className="text-surface-500">Discount:</span> ${Number(b.discountAmount || 0).toFixed(2)}</p>
                    <p><span className="text-surface-500">Paid:</span> ${Number(b.amountPaid || 0).toFixed(2)}</p>
                    <p><span className="text-surface-500">Claim status:</span> {b.claimStatus?.replace(/_/g, ' ')}</p>
                    {b.insuranceProvider && (
                      <p><span className="text-surface-500">Insurance:</span> {b.insuranceProvider} ({b.insurancePolicyNumber})</p>
                    )}
                  </div>

                  {b.items?.length > 0 && (
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="text-left text-surface-500">
                          <th className="pb-2 font-medium">Description</th>
                          <th className="pb-2 font-medium">Qty</th>
                          <th className="pb-2 font-medium text-right">Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {b.items.map((it) => (
                          <tr key={it.id}>
                            <td className="py-1">{it.description}</td>
                            <td className="py-1">{it.quantity}</td>
                            <td className="py-1 text-right tabular-nums">${Number(it.totalPrice).toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}

                  {b.payments?.length > 0 && (
                    <div>
                      <p className="text-sm font-medium text-surface-700 mb-2">Payments</p>
                      {b.payments.map((p) => (
                        <p key={p.id} className="text-sm text-surface-500">
                          ${Number(p.amount).toFixed(2)} via {p.paymentMethod} on {p.paymentDate?.slice(0, 10)}
                        </p>
                      ))}
                    </div>
                  )}

                  {['PENDING', 'PARTIALLY_PAID', 'OVERDUE'].includes(b.status) && (
                    payForm === b.id ? (
                      <div className="flex flex-wrap gap-2 items-end">
                        <div>
                          <label className="label">Amount</label>
                          <input type="number" step="0.01" className="input w-32" value={payAmount} onChange={(e) => setPayAmount(e.target.value)} />
                        </div>
                        <div>
                          <label className="label">Method</label>
                          <select className="input w-32" value={payMethod} onChange={(e) => setPayMethod(e.target.value)}>
                            <option value="CARD">Card</option>
                            <option value="CASH">Cash</option>
                            <option value="UPI">UPI</option>
                            <option value="BANK_TRANSFER">Bank transfer</option>
                          </select>
                        </div>
                        <button className="btn-primary" disabled={paying} onClick={() => recordPayment(b.id)}>
                          {paying ? 'Processing…' : 'Pay'}
                        </button>
                        <button className="btn-ghost" onClick={() => setPayForm(null)}>Cancel</button>
                      </div>
                    ) : (
                      <button className="btn-primary text-sm" onClick={() => { setPayForm(b.id); setPayAmount((Number(b.totalAmount) - Number(b.amountPaid || 0)).toFixed(2)) }}>
                        Record payment
                      </button>
                    )
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {user?.role !== 'PATIENT' && !showCreate && billings.length === 0 && (
        <div className="card p-8 text-center text-surface-500 text-sm">
          Create an invoice for a patient using the button above.
        </div>
      )}
    </div>
  )
}
