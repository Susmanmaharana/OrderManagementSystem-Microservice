import apiClient from './apiClient';
import API_CONFIG from '../config/apiConfig';

const base = () => API_CONFIG.PAYMENT_SERVICE;

export async function createPayment(body, headers = {}) {
  const { data } = await apiClient.post(`${base()}/api/v1/payments`, body, { headers });
  return data;
}

export async function getPaymentById(paymentId) {
  const { data } = await apiClient.get(`${base()}/api/v1/payments/${paymentId}`);
  return data;
}

export async function checkPaymentHealth() {
  const { data } = await apiClient.get(`${base()}/health`);
  return data;
}
