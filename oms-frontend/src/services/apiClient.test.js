/**
 * @jest-environment node
 */
import apiClient from '../services/apiClient';

test('api client maps network errors to friendly messages', async () => {
  const error = {
    config: { url: 'http://localhost:8081/api/v1/orders' },
    response: undefined,
    code: 'ERR_NETWORK',
  };

  // Trigger interceptor by simulating reject path
  const handlers = apiClient.interceptors.response.handlers[0];
  await expect(handlers.rejected(error)).rejects.toMatchObject({
    status: 0,
    message: expect.stringContaining('Order Service'),
  });
});

test('api client maps 404 without body message', async () => {
  const error = {
    config: { url: 'http://localhost:8083/api/v1/payments/x' },
    response: { status: 404, data: {} },
  };
  const handlers = apiClient.interceptors.response.handlers[0];
  await expect(handlers.rejected(error)).rejects.toMatchObject({
    status: 404,
    message: 'Resource not found.',
  });
});
