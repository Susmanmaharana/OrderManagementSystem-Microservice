import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createOrder } from '../../services/orderService';
import ErrorMessage from '../common/ErrorMessage';

const emptyItem = () => ({ productId: '101', quantity: '1' });

function CreateOrder() {
  const navigate = useNavigate();
  const [customerId, setCustomerId] = useState('1001');
  const [items, setItems] = useState([emptyItem()]);
  const [forceFail, setForceFail] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validation, setValidation] = useState('');

  function updateItem(index, field, value) {
    setItems((prev) => prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)));
  }

  function addItem() {
    setItems((prev) => [...prev, emptyItem()]);
  }

  function removeItem(index) {
    setItems((prev) => (prev.length <= 1 ? prev : prev.filter((_, i) => i !== index)));
  }

  function validate() {
    if (!customerId || Number(customerId) <= 0) return 'Customer ID is required.';
    if (!items.length) return 'At least one item is required.';
    for (const item of items) {
      if (!item.productId || Number(item.productId) <= 0) return 'Each item needs a Product ID.';
      if (!item.quantity || Number(item.quantity) <= 0) return 'Quantity must be greater than 0.';
    }
    return '';
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const msg = validate();
    setValidation(msg);
    setError('');
    if (msg) return;

    setLoading(true);
    try {
      const payload = {
        customerId: Number(customerId),
        items: items.map((item) => ({
          productId: Number(item.productId),
          quantity: Number(item.quantity),
        })),
      };
      const order = await createOrder(payload, { forcePaymentFailure: forceFail });
      navigate(`/orders/${order.orderId}`);
    } catch (err) {
      setError(err.message || 'Could not create order.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="panel form" onSubmit={handleSubmit}>
      <h3>Create order</h3>
      <ErrorMessage message={validation || error} />

      <label htmlFor="customerId">Customer ID</label>
      <input
        id="customerId"
        type="number"
        min="1"
        value={customerId}
        onChange={(e) => setCustomerId(e.target.value)}
        disabled={loading}
        required
      />

      <fieldset className="items-fieldset" disabled={loading}>
        <legend>Order items</legend>
        {items.map((item, index) => (
          <div className="item-row" key={index}>
            <label>
              Product ID
              <input
                type="number"
                min="1"
                value={item.productId}
                onChange={(e) => updateItem(index, 'productId', e.target.value)}
                required
              />
            </label>
            <label>
              Quantity
              <input
                type="number"
                min="1"
                value={item.quantity}
                onChange={(e) => updateItem(index, 'quantity', e.target.value)}
                required
              />
            </label>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => removeItem(index)}
              disabled={items.length <= 1 || loading}
            >
              Remove
            </button>
          </div>
        ))}
        <button type="button" className="btn btn-secondary" onClick={addItem} disabled={loading}>
          Add item
        </button>
      </fieldset>

      <label className="checkbox-row">
        <input
          type="checkbox"
          checked={forceFail}
          onChange={(e) => setForceFail(e.target.checked)}
          disabled={loading}
        />
        Force payment failure (saga demo)
      </label>

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Creating order...' : 'Create order'}
      </button>
    </form>
  );
}

export default CreateOrder;
