import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InventoryPage from '../pages/InventoryPage';
import * as inventoryService from '../services/inventoryService';

jest.mock('../services/inventoryService');

test('inventory search loads product details', async () => {
  const user = userEvent.setup();
  inventoryService.getInventory.mockResolvedValue({
    productId: 101,
    availableQuantity: 50,
    version: 0,
  });

  render(<InventoryPage />);

  await user.click(screen.getByRole('button', { name: /search/i }));

  await waitFor(() => {
    expect(inventoryService.getInventory).toHaveBeenCalledWith(101);
  });
  expect(await screen.findByText(/stock details/i)).toBeInTheDocument();
  expect(screen.getByText('50')).toBeInTheDocument();
});

test('inventory search shows api error', async () => {
  const user = userEvent.setup();
  inventoryService.getInventory.mockRejectedValue({
    message: 'Inventory Service is currently unavailable. Please try again.',
  });

  render(<InventoryPage />);
  await user.click(screen.getByRole('button', { name: /search/i }));

  expect(await screen.findByRole('alert')).toHaveTextContent(/inventory service is currently unavailable/i);
});
