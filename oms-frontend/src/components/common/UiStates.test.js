import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import OrderList from '../order/OrderList';
import EmptyState from './EmptyState';
import Loading from './Loading';
import ErrorMessage from './ErrorMessage';
import PaymentDetails from '../payment/PaymentDetails';
import NotificationList from '../notification/NotificationList';
import InventoryDetails from '../inventory/InventoryDetails';

test('order list empty state', () => {
  render(
    <MemoryRouter>
      <OrderList orders={[]} />
    </MemoryRouter>
  );
  expect(screen.getByText(/no orders found/i)).toBeInTheDocument();
});

test('order list renders rows', () => {
  render(
    <MemoryRouter>
      <OrderList
        orders={[
          {
            orderId: 1,
            customerId: 1001,
            status: 'CONFIRMED',
            totalAmount: 100,
            createdAt: '2026-09-20T10:00:00',
          },
        ]}
      />
    </MemoryRouter>
  );
  expect(screen.getByText('1')).toBeInTheDocument();
  expect(screen.getByText('CONFIRMED')).toBeInTheDocument();
  expect(screen.getByRole('link', { name: /details/i })).toHaveAttribute('href', '/orders/1');
});

test('empty state and loading and error message', () => {
  const { rerender } = render(<EmptyState message="No inventory information available." />);
  expect(screen.getByText(/no inventory information available/i)).toBeInTheDocument();

  rerender(<Loading label="Loading orders..." />);
  expect(screen.getByText(/loading orders/i)).toBeInTheDocument();

  rerender(<ErrorMessage message="Order Service is currently unavailable. Please try again." />);
  expect(screen.getByRole('alert')).toHaveTextContent(/order service is currently unavailable/i);
});

test('inventory details display', () => {
  render(
    <InventoryDetails inventory={{ productId: 101, availableQuantity: 50, version: 2 }} />
  );
  expect(screen.getByText('101')).toBeInTheDocument();
  expect(screen.getByText('50')).toBeInTheDocument();
});

test('payment details display', () => {
  render(
    <PaymentDetails
      payment={{ paymentId: 'PAY-00001', orderId: 9, amount: 100, status: 'SUCCESS' }}
    />
  );
  expect(screen.getByText('PAY-00001')).toBeInTheDocument();
  expect(screen.getByText('SUCCESS')).toBeInTheDocument();
});

test('notification list display and empty', () => {
  const { rerender } = render(<NotificationList notifications={[]} />);
  expect(screen.getByText(/no notifications available/i)).toBeInTheDocument();

  rerender(
    <NotificationList
      notifications={[
        {
          eventId: 'e1',
          orderId: 1,
          eventType: 'ORDER_CONFIRMED',
          customerId: 1001,
          processedAt: '2026-09-20T10:21:00',
          message: 'ok',
        },
      ]}
    />
  );
  expect(screen.getByText('ORDER_CONFIRMED')).toBeInTheDocument();
  expect(screen.getByText('ok')).toBeInTheDocument();
});
