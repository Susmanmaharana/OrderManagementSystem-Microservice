import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getOrderById, getOrdersByCustomer } from '../services/orderService';
import OrderList from '../components/order/OrderList';
import ErrorMessage from '../components/common/ErrorMessage';
import Loading from '../components/common/Loading';

function OrdersPage() {
  const [customerId, setCustomerId] = useState('1001');
  const [orderId, setOrderId] = useState('');
  const [orders, setOrders] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function searchByCustomer(event) {
    event.preventDefault();
    setError('');
    if (!customerId || Number(customerId) <= 0) {
      setError('Enter a valid customer ID.');
      return;
    }
    setLoading(true);
    try {
      const data = await getOrdersByCustomer(Number(customerId));
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      setOrders([]);
      setError(err.message || 'Could not load orders.');
    } finally {
      setLoading(false);
    }
  }

  async function searchByOrderId(event) {
    event.preventDefault();
    setError('');
    if (!orderId || Number(orderId) <= 0) {
      setError('Enter a valid order ID.');
      return;
    }
    setLoading(true);
    try {
      const data = await getOrderById(Number(orderId));
      setOrders(data ? [data] : []);
    } catch (err) {
      setOrders([]);
      setError(err.message || 'Order not found.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h2>Orders</h2>
        <Link className="btn btn-primary" to="/orders/new">
          Create order
        </Link>
      </div>

      <div className="search-row">
        <form className="inline-form" onSubmit={searchByCustomer}>
          <label htmlFor="listCustomerId">Customer ID</label>
          <input
            id="listCustomerId"
            type="number"
            min="1"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary" disabled={loading}>
            List by customer
          </button>
        </form>

        <form className="inline-form" onSubmit={searchByOrderId}>
          <label htmlFor="searchOrderId">Order ID</label>
          <input
            id="searchOrderId"
            type="number"
            min="1"
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary" disabled={loading}>
            Find by ID
          </button>
        </form>
      </div>

      <ErrorMessage message={error} />
      {loading && <Loading label="Loading orders..." />}
      {!loading && orders !== null && <OrderList orders={orders} />}
      {!loading && orders === null && (
        <p className="muted">Search by customer ID or order ID to view orders.</p>
      )}
    </section>
  );
}

export default OrdersPage;
