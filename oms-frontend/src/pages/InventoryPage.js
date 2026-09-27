import { useState } from 'react';
import { getInventory, upsertInventory } from '../services/inventoryService';
import InventoryDetails from '../components/inventory/InventoryDetails';
import InventoryActionForm from '../components/inventory/InventoryActionForm';
import ErrorMessage from '../components/common/ErrorMessage';
import SuccessMessage from '../components/common/SuccessMessage';
import Loading from '../components/common/Loading';
import EmptyState from '../components/common/EmptyState';
import ConfirmDialog from '../components/common/ConfirmDialog';

function InventoryPage() {
  const [productId, setProductId] = useState('101');
  const [inventory, setInventory] = useState(null);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [setQty, setSetQty] = useState('50');
  const [upsertLoading, setUpsertLoading] = useState(false);
  const [confirmUpsert, setConfirmUpsert] = useState(false);

  async function load(id) {
    setLoading(true);
    setError('');
    setSuccess('');
    setSearched(true);
    try {
      const data = await getInventory(id);
      setInventory(data);
      if (data && data.availableQuantity != null) {
        setSetQty(String(data.availableQuantity));
      }
    } catch (err) {
      setInventory(null);
      setError(err.message || 'No inventory information available.');
    } finally {
      setLoading(false);
    }
  }

  async function handleSearch(event) {
    event.preventDefault();
    if (!productId || Number(productId) <= 0) {
      setError('Enter a valid product ID.');
      return;
    }
    await load(Number(productId));
  }

  function requestUpsert(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    if (!productId || Number(productId) <= 0) {
      setError('Enter a valid product ID.');
      return;
    }
    if (setQty === '' || Number(setQty) < 0) {
      setError('Quantity must be 0 or greater.');
      return;
    }
    setConfirmUpsert(true);
  }

  async function runUpsert() {
    setUpsertLoading(true);
    setError('');
    try {
      const data = await upsertInventory(Number(productId), Number(setQty));
      setInventory(data);
      setSearched(true);
      setSuccess(`Quantity for product ${productId} set to ${setQty}.`);
      setConfirmUpsert(false);
    } catch (err) {
      setError(err.message || 'Could not update inventory.');
    } finally {
      setUpsertLoading(false);
    }
  }

  function refreshFromAction(result) {
    if (result && result.productId != null) {
      load(result.productId);
    }
  }

  return (
    <section>
      <div className="page-header">
        <h2>Inventory</h2>
        <button
          type="button"
          className="btn btn-secondary"
          disabled={loading || !productId}
          onClick={() => productId && Number(productId) > 0 && load(Number(productId))}
        >
          Refresh
        </button>
      </div>
      <p className="muted">Search stock, adjust quantity, reserve, or release for an order line.</p>

      <form className="panel inline-form" onSubmit={handleSearch} style={{ padding: '1rem' }}>
        <label htmlFor="searchProductId">Product ID</label>
        <input
          id="searchProductId"
          type="number"
          min="1"
          value={productId}
          onChange={(e) => setProductId(e.target.value)}
          disabled={loading}
        />
        <button type="submit" className="btn btn-primary" disabled={loading}>
          Search
        </button>
      </form>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />
      {loading && <Loading label="Loading inventory..." />}
      {!loading && searched && !inventory && !error && (
        <EmptyState message="No inventory information available." />
      )}
      {!loading && inventory && <InventoryDetails inventory={inventory} />}

      <form className="panel form" onSubmit={requestUpsert}>
        <h3>Set available quantity</h3>
        <p className="muted">Admin-style upsert for demos (PUT /api/v1/inventory/&#123;productId&#125;).</p>
        <label htmlFor="setQty">Available quantity</label>
        <input
          id="setQty"
          type="number"
          min="0"
          value={setQty}
          onChange={(e) => setSetQty(e.target.value)}
          disabled={upsertLoading}
        />
        <button type="submit" className="btn btn-secondary" disabled={upsertLoading}>
          {upsertLoading ? 'Saving...' : 'Save quantity'}
        </button>
        <ConfirmDialog
          open={confirmUpsert}
          busy={upsertLoading}
          title="Update stock?"
          message={`Set available quantity of product ${productId} to ${setQty}?`}
          confirmLabel="Yes, save"
          onConfirm={runUpsert}
          onCancel={() => setConfirmUpsert(false)}
        />
      </form>

      <div className="split-panels">
        <InventoryActionForm
          mode="reserve"
          defaultProductId={inventory ? inventory.productId : productId}
          onSuccess={refreshFromAction}
        />
        <InventoryActionForm
          mode="release"
          defaultProductId={inventory ? inventory.productId : productId}
          onSuccess={refreshFromAction}
        />
      </div>
    </section>
  );
}

export default InventoryPage;
