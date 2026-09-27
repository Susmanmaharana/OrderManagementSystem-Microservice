import StatusBadge from '../common/StatusBadge';
import EmptyState from '../common/EmptyState';

function NotificationList({ notifications }) {
  if (!notifications || notifications.length === 0) {
    return <EmptyState message="No notifications available." />;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Order ID</th>
            <th>Event</th>
            <th>Customer</th>
            <th>Time</th>
            <th>Message</th>
          </tr>
        </thead>
        <tbody>
          {notifications.map((n) => (
            <tr key={n.eventId}>
              <td>{n.orderId != null ? n.orderId : '—'}</td>
              <td>
                <StatusBadge status={n.eventType} />
              </td>
              <td>{n.customerId != null ? n.customerId : '—'}</td>
              <td>{n.processedAt ? String(n.processedAt).replace('T', ' ').slice(0, 19) : '—'}</td>
              <td className="msg-cell">{n.message || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default NotificationList;
