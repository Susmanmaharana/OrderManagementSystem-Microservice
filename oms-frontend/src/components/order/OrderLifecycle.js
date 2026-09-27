function OrderLifecycle({ status }) {
  const happy = ['CREATED', 'INVENTORY_RESERVED', 'PAYMENT_PENDING', 'CONFIRMED'];
  const failed = status === 'PAYMENT_FAILED';
  const cancelled = status === 'CANCELLED';

  const steps = failed
    ? ['CREATED', 'INVENTORY_RESERVED', 'PAYMENT_PENDING', 'PAYMENT_FAILED']
    : cancelled
      ? ['CREATED', 'CANCELLED']
      : happy;

  const currentIndex = Math.max(0, steps.indexOf(status));

  return (
    <div className="lifecycle" aria-label="Order lifecycle">
      {steps.map((step, index) => {
        const reached = index <= currentIndex;
        return (
          <div key={step} className="lifecycle-step-wrap">
            <div className={`lifecycle-step ${reached ? 'reached' : ''}`}>
              <span className="lifecycle-label">{step.replace(/_/g, ' ')}</span>
            </div>
            {index < steps.length - 1 && <div className={`lifecycle-arrow ${reached ? 'reached' : ''}`}>↓</div>}
          </div>
        );
      })}
    </div>
  );
}

export default OrderLifecycle;
