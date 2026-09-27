function InventoryDetails({ inventory }) {
  if (!inventory) return null;

  return (
    <div className="panel">
      <h3>Stock details</h3>
      <dl className="detail-grid">
        <div>
          <dt>Product ID</dt>
          <dd>{inventory.productId}</dd>
        </div>
        <div>
          <dt>Available quantity</dt>
          <dd>{inventory.availableQuantity}</dd>
        </div>
        <div>
          <dt>Version</dt>
          <dd>{inventory.version != null ? inventory.version : '—'}</dd>
        </div>
      </dl>
      <p className="muted" style={{ marginBottom: 0 }}>
        Seeded demo products: 101, 102, 103
      </p>
    </div>
  );
}

export default InventoryDetails;
