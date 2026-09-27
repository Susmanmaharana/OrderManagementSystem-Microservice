import { useState } from 'react';
import { createPayment } from '../../services/paymentService';
import ErrorMessage from '../common/ErrorMessage';
import PaymentDetails from './PaymentDetails';

function PaymentForm({ onCreated }) {
  const [orderId, setOrderId] = useState('');
  const [amount, setAmount] = useState('100.00');
  const [idempotencyKey, setIdempotencyKey] = useState('');
  const [forceFail, setForceFail] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setResult(null);

    if (!orderId || Number(orderId) <= 0) {
      setError('Order ID is required.');
      return;
    }
    if (!amount || Number(amount) < 0.01) {
      setError('Amount must be at least 0.01.');
      return;
    }

    const body = {
      orderId: Number(orderId),
      amount: Number(amount),
    };
    if (idempotencyKey.trim()) {
      body.idempotencyKey = idempotencyKey.trim();
    }

    const headers = {};
    if (forceFail) {
      headers['X-Force-Payment-Failure'] = 'true';
    }

    setLoading(true);
    try {
      const data = await createPayment(body, headers);
      setResult(data);
      if (onCreated) onCreated(data);
    } catch (err) {
      setError(err.message || 'Could not process payment.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="panel form" onSubmit={handleSubmit}>
      <h3>Create payment</h3>
      <p className="muted">
        Simulated charge. Usually done by Order Service during the saga; use this for direct demos.
      </p>
      <ErrorMessage message={error} />

      <label htmlFor="pay-orderId">Order ID</label>
      <input
        id="pay-orderId"
        type="number"
        min="1"
        value={orderId}
        onChange={(e) => setOrderId(e.target.value)}
        disabled={loading}
        required
      />

      <label htmlFor="pay-amount">Amount</label>
      <input
        id="pay-amount"
        type="number"
        min="0.01"
        step="0.01"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        disabled={loading}
        required
      />

      <label htmlFor="pay-idem">Idempotency key (optional)</label>
      <input
        id="pay-idem"
        type="text"
        value={idempotencyKey}
        onChange={(e) => setIdempotencyKey(e.target.value)}
        placeholder="e.g. pay-demo-001"
        disabled={loading}
      />

      <label className="checkbox-row">
        <input
          type="checkbox"
          checked={forceFail}
          onChange={(e) => setForceFail(e.target.checked)}
          disabled={loading}
        />
        Force payment failure
      </label>

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Processing...' : 'Create payment'}
      </button>

      {result && (
        <div className="result-box">
          <PaymentDetails payment={result} />
        </div>
      )}
    </form>
  );
}

export default PaymentForm;
