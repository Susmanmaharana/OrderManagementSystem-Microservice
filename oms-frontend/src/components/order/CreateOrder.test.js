import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import CreateOrder from './CreateOrder';
import * as orderService from '../../services/orderService';

jest.mock('../../services/orderService');

const renderForm = () =>
  render(
    <MemoryRouter>
      <CreateOrder />
    </MemoryRouter>
  );

test('create order validates empty customer id', async () => {
  const user = userEvent.setup();
  renderForm();

  await user.clear(screen.getByLabelText(/customer id/i));
  await user.click(screen.getByRole('button', { name: /create order/i }));

  expect(await screen.findByRole('alert')).toHaveTextContent(/customer id is required/i);
  expect(orderService.createOrder).not.toHaveBeenCalled();
});

test('create order validates quantity', async () => {
  const user = userEvent.setup();
  renderForm();

  const qtyInputs = screen.getAllByLabelText(/quantity/i);
  await user.clear(qtyInputs[0]);
  await user.type(qtyInputs[0], '0');
  await user.click(screen.getByRole('button', { name: /create order/i }));

  expect(await screen.findByRole('alert')).toHaveTextContent(/quantity must be greater than 0/i);
});

test('create order calls service and shows loading label', async () => {
  const user = userEvent.setup();
  let resolveCreate;
  orderService.createOrder.mockReturnValue(
    new Promise((resolve) => {
      resolveCreate = resolve;
    })
  );

  renderForm();
  await user.click(screen.getByRole('button', { name: /create order/i }));

  expect(await screen.findByRole('button', { name: /creating order/i })).toBeDisabled();

  resolveCreate({ orderId: 42, status: 'CONFIRMED' });
  await waitFor(() => expect(orderService.createOrder).toHaveBeenCalled());
});
