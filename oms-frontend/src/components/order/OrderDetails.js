import { useState } from 'react';
import { cancelOrder } from '../../services/orderService';
import ErrorMessage from '../common/ErrorMessage';
import SuccessMessage from '../common/SuccessMessage';
import StatusBadge from '../common/StatusBadge';
import ConfirmDialog from '../common/ConfirmDialog';
import OrderLifecycle from './OrderLifecycle';

const CANCELLABLE = new Set(['CREATED', 'INVENTORY_RESERVED', 'PAYMENT_PENDING']);

function OrderDetails({ order, onUpdated }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);

  if (!order) return null;

  const canCancel = CANCELLABLE.has(order.status);

  async function handleCancel() {
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      const updated = await cancelOrder(order.orderId);
      setConfirmOpen(false);
      setSuccess(`Order #${order.orderId} cancelled.`);
      if (onUpdated) onUpdated(updated);
    } catch (err) {
      setError(err.message || 'Could not cancel order.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="panel">
      <div className="panel-header">
        <h3>Order #{order.orderId}</h3>
        <StatusBadge status={order.status} />
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      <dl className="detail-grid">
        <div>
          <dt>Customer ID</dt>
          <dd>{order.customerId}</dd>
        </div>
        <div>
          <dt>Total amount</dt>
          <dd>{order.totalAmount != null ? Number(order.totalAmount).toFixed(2) : '—'}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{order.createdAt ? String(order.createdAt).replace('T', ' ').slice(0, 19) : '—'}</dd>
        </div>
        <div>
          <dt>Status</dt>
          <dd>{order.status}</dd>
        </div>
      </dl>

      <h4>Items</h4>
      {(order.items || []).length === 0 ? (
        <p className="empty-state">No line items on this order.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Product ID</th>
                <th>Quantity</th>
                <th>Price</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item, index) => (
                <tr key={`${item.productId}-${index}`}>
                  <td>{item.productId}</td>
                  <td>{item.quantity}</td>
                  <td>{item.price != null ? Number(item.price).toFixed(2) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <h4>Lifecycle</h4>
      <OrderLifecycle status={order.status} />

      {canCancel && (
        <div className="actions">
          {!confirmOpen ? (
            <button type="button" className="btn btn-danger" onClick={() => setConfirmOpen(true)} disabled={loading}>
              Cancel order
            </button>
          ) : (
            <ConfirmDialog
              open
              danger
              busy={loading}
              title="Cancel order?"
              message={`Cancel order #${order.orderId}? Reserved stock will be released if needed.`}
              confirmLabel="Yes, cancel"
              cancelLabel="Keep order"
              onConfirm={handleCancel}
              onCancel={() => setConfirmOpen(false)}
            />
          )}
        </div>
      )}
    </div>
  );
}

export default OrderDetails;
