import { useState } from 'react';
import { getPaymentById } from '../services/paymentService';
import PaymentForm from '../components/payment/PaymentForm';
import PaymentDetails from '../components/payment/PaymentDetails';
import ErrorMessage from '../components/common/ErrorMessage';
import SuccessMessage from '../components/common/SuccessMessage';
import Loading from '../components/common/Loading';
import EmptyState from '../components/common/EmptyState';

function PaymentsPage() {
  const [paymentId, setPaymentId] = useState('');
  const [payment, setPayment] = useState(null);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function findPayment(id) {
    setLoading(true);
    setError('');
    setSuccess('');
    setSearched(true);
    try {
      const data = await getPaymentById(id);
      setPayment(data);
    } catch (err) {
      setPayment(null);
      setError(err.message || 'Payment not found.');
    } finally {
      setLoading(false);
    }
  }

  async function handleSearch(event) {
    event.preventDefault();
    const id = paymentId.trim();
    if (!id) {
      setError('Enter a payment ID (e.g. PAY-00001).');
      setPayment(null);
      setSearched(true);
      return;
    }
    await findPayment(id);
  }

  function handleCreated(data) {
    if (data && data.paymentId) {
      setPaymentId(data.paymentId);
      setPayment(data);
      setSearched(true);
      setError('');
      setSuccess(`Payment ${data.paymentId} created (${data.status}).`);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h2>Payments</h2>
        <button
          type="button"
          className="btn btn-secondary"
          disabled={loading || !paymentId.trim()}
          onClick={() => paymentId.trim() && findPayment(paymentId.trim())}
        >
          Refresh
        </button>
      </div>
      <p className="muted">
        Create a payment or look up by payment ID. Lookup by order ID is not available on the backend.
      </p>

      <form className="panel inline-form" onSubmit={handleSearch} style={{ padding: '1rem' }}>
        <label htmlFor="searchPaymentId">Payment ID</label>
        <input
          id="searchPaymentId"
          type="text"
          value={paymentId}
          onChange={(e) => setPaymentId(e.target.value)}
          placeholder="PAY-00001"
          style={{ maxWidth: 200 }}
          disabled={loading}
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          Find
        </button>
      </form>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />
      {loading && <Loading label="Loading payment..." />}
      {!loading && searched && !payment && !error && <EmptyState message="No payments found." />}
      {!loading && payment && (
        <div className="panel">
          <PaymentDetails payment={payment} />
        </div>
      )}

      <PaymentForm onCreated={handleCreated} />
    </section>
  );
}

export default PaymentsPage;
