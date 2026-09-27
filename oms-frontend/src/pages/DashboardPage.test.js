import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from './DashboardPage';

jest.mock('../services/orderService', () => ({
  getOrdersByCustomer: jest.fn().mockResolvedValue([]),
  checkOrderHealth: jest.fn().mockResolvedValue({ status: 'UP' }),
}));

jest.mock('../services/inventoryService', () => ({
  getInventory: jest.fn().mockResolvedValue({ productId: 101, availableQuantity: 50, version: 0 }),
  checkInventoryHealth: jest.fn().mockResolvedValue({ status: 'UP' }),
}));

jest.mock('../services/paymentService', () => ({
  checkPaymentHealth: jest.fn().mockResolvedValue({ status: 'UP' }),
}));

jest.mock('../services/notificationService', () => ({
  getNotifications: jest.fn().mockResolvedValue([]),
  checkNotificationHealth: jest.fn().mockResolvedValue({ status: 'UP' }),
}));

test('dashboard renders summary cards', async () => {
  render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );

  expect(screen.getByRole('heading', { name: /dashboard/i })).toBeInTheDocument();
  expect(await screen.findByText(/total orders/i)).toBeInTheDocument();
  expect(screen.getByText(/service status/i)).toBeInTheDocument();
});
