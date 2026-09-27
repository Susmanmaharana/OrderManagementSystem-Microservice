import apiClient from './apiClient';
import API_CONFIG from '../config/apiConfig';

const base = () => API_CONFIG.ORDER_SERVICE;

export async function createOrder(orderData, options = {}) {
  const headers = {};
  if (options.forcePaymentFailure) {
    headers['X-Force-Payment-Failure'] = 'true';
  }
  if (options.idempotencyKey) {
    headers['Idempotency-Key'] = options.idempotencyKey;
  }
  const { data } = await apiClient.post(`${base()}/api/v1/orders`, orderData, { headers });
  return data;
}

export async function getOrderById(orderId) {
  const { data } = await apiClient.get(`${base()}/api/v1/orders/${orderId}`);
  return data;
}

export async function getOrdersByCustomer(customerId) {
  const { data } = await apiClient.get(`${base()}/api/v1/orders`, {
    params: { customerId },
  });
  return data;
}

export async function cancelOrder(orderId) {
  const { data } = await apiClient.put(`${base()}/api/v1/orders/${orderId}/cancel`);
  return data;
}

export async function checkOrderHealth() {
  const { data } = await apiClient.get(`${base()}/actuator/health`);
  return data;
}
