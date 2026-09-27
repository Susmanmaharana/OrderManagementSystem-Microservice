import { useEffect, useState } from 'react';
import { reserveInventory, releaseInventory } from '../../services/inventoryService';
import ErrorMessage from '../common/ErrorMessage';
import SuccessMessage from '../common/SuccessMessage';
import StatusBadge from '../common/StatusBadge';
import ConfirmDialog from '../common/ConfirmDialog';

function InventoryActionForm({ mode, defaultProductId, onSuccess }) {
  const isReserve = mode === 'reserve';
  const [orderId, setOrderId] = useState('');
  const [productId, setProductId] = useState(defaultProductId ? String(defaultProductId) : '101');
  const [quantity, setQuantity] = useState('1');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [result, setResult] = useState(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  useEffect(() => {
    if (defaultProductId != null && defaultProductId !== '') {
      setProductId(String(defaultProductId));
    }
  }, [defaultProductId]);

  function validate() {
    if (!orderId || Number(orderId) <= 0) return 'Order ID is required.';
    if (!productId || Number(productId) <= 0) return 'Product ID is required.';
    if (!quantity || Number(quantity) <= 0) return 'Quantity must be greater than 0.';
    return '';
  }

  function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setResult(null);
    const msg = validate();
    if (msg) {
      setError(msg);
      return;
    }
    setConfirmOpen(true);
  }

  async function runAction() {
    const body = {
      orderId: Number(orderId),
      productId: Number(productId),
      quantity: Number(quantity),
    };

    setLoading(true);
    setError('');
    try {
      const data = isReserve ? await reserveInventory(body) : await releaseInventory(body);
      setResult(data);
      setSuccess(isReserve ? 'Stock reserved.' : 'Stock released.');
      setConfirmOpen(false);
      if (onSuccess) onSuccess(data);
    } catch (err) {
      setError(err.message || `Could not ${mode} inventory.`);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="panel form" onSubmit={handleSubmit}>
      <h3>{isReserve ? 'Reserve inventory' : 'Release inventory'}</h3>
      <p className="muted">
        {isReserve
          ? 'Holds stock for an order (idempotent per order + product).'
          : 'Returns previously reserved stock for an order line.'}
      </p>
      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      <label htmlFor={`${mode}-orderId`}>Order ID</label>
      <input
        id={`${mode}-orderId`}
        type="number"
        min="1"
        value={orderId}
        onChange={(e) => setOrderId(e.target.value)}
        disabled={loading}
        required
      />

      <label htmlFor={`${mode}-productId`}>Product ID</label>
      <input
        id={`${mode}-productId`}
        type="number"
        min="1"
        value={productId}
        onChange={(e) => setProductId(e.target.value)}
        disabled={loading}
        required
      />

      <label htmlFor={`${mode}-quantity`}>Quantity</label>
      <input
        id={`${mode}-quantity`}
        type="number"
        min="1"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
        disabled={loading}
        required
      />

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? (isReserve ? 'Reserving...' : 'Releasing...') : isReserve ? 'Reserve' : 'Release'}
      </button>

      <ConfirmDialog
        open={confirmOpen}
        busy={loading}
        title={isReserve ? 'Confirm reserve' : 'Confirm release'}
        message={`${isReserve ? 'Reserve' : 'Release'} ${quantity} unit(s) of product ${productId} for order ${orderId}?`}
        confirmLabel={isReserve ? 'Yes, reserve' : 'Yes, release'}
        onConfirm={runAction}
        onCancel={() => setConfirmOpen(false)}
      />

      {result && (
        <div className="result-box">
          <p>
            Status: <StatusBadge status={result.status} />
          </p>
          <p>
            Order {result.orderId} · Product {result.productId} · Qty {result.quantity}
          </p>
          <p>Available now: {result.availableQuantity}</p>
        </div>
      )}
    </form>
  );
}

export default InventoryActionForm;
