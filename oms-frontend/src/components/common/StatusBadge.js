const LABELS = {
  CREATED: 'CREATED',
  INVENTORY_RESERVED: 'INVENTORY_RESERVED',
  PAYMENT_PENDING: 'PAYMENT_PENDING',
  CONFIRMED: 'CONFIRMED',
  PAYMENT_FAILED: 'PAYMENT_FAILED',
  CANCELLED: 'CANCELLED',
};

function StatusBadge({ status }) {
  const text = LABELS[status] || status || 'UNKNOWN';
  const tone = String(status || '').toLowerCase().replace(/_/g, '-');
  return (
    <span className={`status-badge status-${tone}`} title={text}>
      {text}
    </span>
  );
}

export default StatusBadge;
