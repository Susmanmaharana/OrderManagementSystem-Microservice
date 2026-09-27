import { useCallback, useEffect, useState } from 'react';
import { getOrdersByCustomer, checkOrderHealth } from '../services/orderService';
import { getInventory, checkInventoryHealth } from '../services/inventoryService';
import { checkPaymentHealth } from '../services/paymentService';
import { getNotifications, checkNotificationHealth } from '../services/notificationService';
import ErrorMessage from '../components/common/ErrorMessage';
import Loading from '../components/common/Loading';

const SEED_PRODUCTS = [101, 102, 103];

function statusLabel(ok) {
  if (ok === true) return { text: 'Connected', className: 'svc-ok' };
  if (ok === false) return { text: 'Down', className: 'svc-down' };
  return { text: 'Unknown', className: 'svc-unknown' };
}

async function probe(fn) {
  try {
    await fn();
    return true;
  } catch (_) {
    return false;
  }
}

function DashboardPage() {
  const [customerId, setCustomerId] = useState('1001');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [stats, setStats] = useState(null);
  const [services, setServices] = useState(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError('');

    const cid = Number(customerId) > 0 ? Number(customerId) : 1001;

    const [orderOk, invOk, payOk, notifOk] = await Promise.all([
      probe(checkOrderHealth),
      probe(checkInventoryHealth),
      probe(checkPaymentHealth),
      probe(checkNotificationHealth),
    ]);

    setServices({
      order: orderOk,
      inventory: invOk,
      payment: payOk,
      notification: notifOk,
    });

    let orders = [];
    let inventoryTotal = null;
    let notificationsCount = null;
    const problems = [];

    try {
      const data = await getOrdersByCustomer(cid);
      orders = Array.isArray(data) ? data : [];
    } catch (err) {
      problems.push(`Orders: ${err.message || 'unavailable'}`);
    }

    try {
      const stocks = await Promise.all(SEED_PRODUCTS.map((id) => getInventory(id).catch(() => null)));
      inventoryTotal = stocks.reduce((sum, row) => {
        if (!row || row.availableQuantity == null) return sum;
        return sum + Number(row.availableQuantity);
      }, 0);
    } catch (err) {
      problems.push(`Inventory: ${err.message || 'unavailable'}`);
    }

    try {
      const notifs = await getNotifications();
      notificationsCount = Array.isArray(notifs) ? notifs.length : 0;
    } catch (err) {
      problems.push(`Notifications: ${err.message || 'unavailable'}`);
    }

    const countStatus = (status) => orders.filter((o) => o.status === status).length;

    setStats({
      customerId: cid,
      totalOrders: orders.length,
      confirmed: countStatus('CONFIRMED'),
      cancelled: countStatus('CANCELLED'),
      pending: orders.filter((o) =>
        ['CREATED', 'INVENTORY_RESERVED', 'PAYMENT_PENDING'].includes(o.status)
      ).length,
      paymentFailed: countStatus('PAYMENT_FAILED'),
      inventoryTotal,
      notificationsCount,
      // No payment list API — approximate from order outcomes for this customer
      paymentsSuccess: countStatus('CONFIRMED'),
      paymentsFailed: countStatus('PAYMENT_FAILED'),
    });

    if (problems.length) {
      setError(problems.join(' · '));
    }

    setLoading(false);
  }, [customerId]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return (
    <section>
      <div className="page-header">
        <h2>Dashboard</h2>
        <button type="button" className="btn btn-secondary" onClick={refresh} disabled={loading}>
          Refresh
        </button>
      </div>

      <p className="muted">
        Totals are computed in the browser from existing APIs (no aggregate backend). Order stats use
        customer ID below. Payment counts are approximated from order statuses (no payment list API).
        Inventory sums seeded products 101–103.
      </p>

      <form
        className="inline-form"
        style={{ marginBottom: '1rem' }}
        onSubmit={(e) => {
          e.preventDefault();
          refresh();
        }}
      >
        <label htmlFor="dashCustomerId">Customer ID</label>
        <input
          id="dashCustomerId"
          type="number"
          min="1"
          value={customerId}
          onChange={(e) => setCustomerId(e.target.value)}
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          Apply
        </button>
      </form>

      <ErrorMessage message={error} />
      {loading && !stats && <Loading label="Loading dashboard..." />}

      {stats && (
        <>
          <div className="card-grid">
            <div className="stat-card">
              <span className="stat-label">Total orders</span>
              <span className="stat-value">{stats.totalOrders}</span>
              <span className="stat-hint">customer {stats.customerId}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Confirmed</span>
              <span className="stat-value">{stats.confirmed}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Pending</span>
              <span className="stat-value">{stats.pending}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Cancelled</span>
              <span className="stat-value">{stats.cancelled}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Available inventory</span>
              <span className="stat-value">
                {stats.inventoryTotal != null ? stats.inventoryTotal : '—'}
              </span>
              <span className="stat-hint">products 101–103</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Payments (approx.)</span>
              <span className="stat-value">
                {stats.paymentsSuccess}/{stats.paymentsFailed}
              </span>
              <span className="stat-hint">success / failed via orders</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Notifications</span>
              <span className="stat-value">
                {stats.notificationsCount != null ? stats.notificationsCount : '—'}
              </span>
            </div>
          </div>

          <h3 style={{ marginTop: '1.5rem' }}>Service status</h3>
          {services && (
            <ul className="svc-list">
              {[
                ['Order Service', services.order],
                ['Inventory Service', services.inventory],
                ['Payment Service', services.payment],
                ['Notification Service', services.notification],
              ].map(([name, ok]) => {
                const s = statusLabel(ok);
                return (
                  <li key={name}>
                    <span>{name}</span>
                    <span className={`svc-pill ${s.className}`}>{s.text}</span>
                  </li>
                );
              })}
            </ul>
          )}
        </>
      )}
    </section>
  );
}

export default DashboardPage;
