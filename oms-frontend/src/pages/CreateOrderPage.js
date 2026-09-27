import { Link } from 'react-router-dom';
import CreateOrder from '../components/order/CreateOrder';

function CreateOrderPage() {
  return (
    <section>
      <div className="page-header">
        <h2>Create order</h2>
        <Link className="btn btn-secondary" to="/orders">
          Back to orders
        </Link>
      </div>
      <p className="muted">Creates an order, reserves inventory, and processes payment (saga).</p>
      <CreateOrder />
    </section>
  );
}

export default CreateOrderPage;
