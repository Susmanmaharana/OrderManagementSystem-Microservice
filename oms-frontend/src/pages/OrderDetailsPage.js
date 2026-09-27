import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getOrderById } from '../services/orderService';
import OrderDetails from '../components/order/OrderDetails';
import ErrorMessage from '../components/common/ErrorMessage';
import Loading from '../components/common/Loading';

function OrderDetailsPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getOrderById(orderId);
      setOrder(data);
    } catch (err) {
      setOrder(null);
      setError(err.message || 'Order not found.');
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <section>
      <div className="page-header">
        <h2>Order details</h2>
        <div className="actions">
          <button type="button" className="btn btn-secondary" onClick={load} disabled={loading}>
            Refresh
          </button>
          <Link className="btn btn-secondary" to="/orders">
            Back to orders
          </Link>
        </div>
      </div>

      <ErrorMessage message={error} />
      {loading && <Loading label="Loading order..." />}
      {!loading && order && <OrderDetails order={order} onUpdated={setOrder} />}
    </section>
  );
}

export default OrderDetailsPage;
