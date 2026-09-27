import StatusBadge from '../common/StatusBadge';

function PaymentDetails({ payment }) {
  if (!payment) return null;

  return (
    <div>
      <h4 style={{ marginTop: 0 }}>Payment</h4>
      <dl className="detail-grid">
        <div>
          <dt>Payment ID</dt>
          <dd>{payment.paymentId}</dd>
        </div>
        <div>
          <dt>Order ID</dt>
          <dd>{payment.orderId}</dd>
        </div>
        <div>
          <dt>Amount</dt>
          <dd>{payment.amount != null ? Number(payment.amount).toFixed(2) : '—'}</dd>
        </div>
        <div>
          <dt>Status</dt>
          <dd>
            <StatusBadge status={payment.status} />
          </dd>
        </div>
      </dl>
    </div>
  );
}

export default PaymentDetails;
