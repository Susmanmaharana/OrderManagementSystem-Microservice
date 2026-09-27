import { useState } from 'react';
import { getNotifications, getNotificationsByOrder } from '../services/notificationService';
import NotificationList from '../components/notification/NotificationList';
import ErrorMessage from '../components/common/ErrorMessage';
import Loading from '../components/common/Loading';

function NotificationsPage() {
  const [orderId, setOrderId] = useState('');
  const [notifications, setNotifications] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [mode, setMode] = useState('all'); // all | order

  async function loadAll() {
    setLoading(true);
    setError('');
    setMode('all');
    try {
      const data = await getNotifications();
      setNotifications(Array.isArray(data) ? data : []);
    } catch (err) {
      setNotifications([]);
      setError(err.message || 'Could not load notifications.');
    } finally {
      setLoading(false);
    }
  }

  async function loadByOrder(event) {
    if (event) event.preventDefault();
    if (!orderId || Number(orderId) <= 0) {
      setError('Enter a valid order ID.');
      return;
    }
    setLoading(true);
    setError('');
    setMode('order');
    try {
      const data = await getNotificationsByOrder(Number(orderId));
      setNotifications(Array.isArray(data) ? data : []);
    } catch (err) {
      setNotifications([]);
      setError(err.message || 'Could not load notifications for order.');
    } finally {
      setLoading(false);
    }
  }

  function refresh() {
    if (mode === 'order' && orderId) {
      loadByOrder();
    } else {
      loadAll();
    }
  }

  return (
    <section>
      <div className="page-header">
        <h2>Notifications</h2>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={refresh}
          disabled={loading || notifications === null}
        >
          Refresh
        </button>
      </div>
      <p className="muted">
        Events consumed from Kafka and stored for idempotency (log-only notifications). Create an
        order first to generate events.
      </p>

      <div className="search-row">
        <button type="button" className="btn btn-primary" onClick={loadAll} disabled={loading}>
          Load all
        </button>
        <form className="inline-form" onSubmit={loadByOrder}>
          <label htmlFor="notifOrderId">Order ID</label>
          <input
            id="notifOrderId"
            type="number"
            min="1"
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            disabled={loading}
          />
          <button type="submit" className="btn btn-secondary" disabled={loading}>
            Filter by order
          </button>
        </form>
      </div>

      <ErrorMessage message={error} />
      {loading && <Loading label="Loading notifications..." />}
      {!loading && notifications !== null && (
        <div className="panel">
          <NotificationList notifications={notifications} />
        </div>
      )}
      {!loading && notifications === null && (
        <p className="muted">Click “Load all” or filter by order ID.</p>
      )}
    </section>
  );
}

export default NotificationsPage;
