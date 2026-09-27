import { Link } from 'react-router-dom';
import StatusBadge from '../common/StatusBadge';
import EmptyState from '../common/EmptyState';

function OrderList({ orders }) {
  if (!orders || orders.length === 0) {
    return <EmptyState message="No orders found." />;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Order ID</th>
            <th>Customer</th>
            <th>Status</th>
            <th>Total</th>
            <th>Created</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.orderId}>
              <td>{order.orderId}</td>
              <td>{order.customerId}</td>
              <td>
                <StatusBadge status={order.status} />
              </td>
              <td>{order.totalAmount != null ? Number(order.totalAmount).toFixed(2) : '—'}</td>
              <td>{order.createdAt ? String(order.createdAt).replace('T', ' ').slice(0, 19) : '—'}</td>
              <td>
                <Link to={`/orders/${order.orderId}`}>Details</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default OrderList;
